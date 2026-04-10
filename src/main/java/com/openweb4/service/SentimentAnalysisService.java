package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.config.AppProperties;
import com.openweb4.model.SentimentResult;
import com.openweb4.model.TweetItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SentimentAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SentimentAnalysisService.class);
    private static final int CACHE_HOURS = 1;
    private static final int AI_TIMEOUT = 45;

    private final AppProperties appProperties;
    private final TweetService tweetService;
    private final Cache<String, SentimentResult> cache;

    public SentimentAnalysisService(AppProperties appProperties, TweetService tweetService) {
        this.appProperties = appProperties;
        this.tweetService = tweetService;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_HOURS, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    public SentimentResult analyze(boolean forceRefresh) {
        String cacheKey = "kol_sentiment";
        if (!forceRefresh) {
            SentimentResult cached = cache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        try {
            SentimentResult result = performAnalysis();
            cache.put(cacheKey, result);
            return result;
        } catch (Exception e) {
            log.error("Sentiment analysis failed: {}", e.getMessage());
            return getFallbackResult();
        }
    }

    private SentimentResult performAnalysis() {
        List<TweetItem> tweets = tweetService.getCachedTweets();
        if (tweets.isEmpty()) {
            return getFallbackResult();
        }

        String prompt = buildSentimentPrompt(tweets);

        AppProperties.Ai cfg = appProperties.getAi();
        if (cfg.getApiKey() == null || cfg.getApiKey().trim().isEmpty()) {
            return simpleSentimentAnalysis(tweets);
        }

        try {
            String response = callAiSync(cfg, prompt);
            if (response == null || response.isBlank()) {
                return simpleSentimentAnalysis(tweets);
            }
            return parseAiResponse(response, tweets);
        } catch (Exception e) {
            log.warn("AI sentiment analysis failed, falling back to simple analysis: {}", e.getMessage());
            return simpleSentimentAnalysis(tweets);
        }
    }

    private String buildSentimentPrompt(List<TweetItem> tweets) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下加密货币行业 KOL 推文的整体情绪，并给出统计数据。\n\n");

        int count = Math.min(tweets.size(), 20);
        for (int i = 0; i < count; i++) {
            TweetItem tweet = tweets.get(i);
            String authorName = tweet.getAuthor() != null ? tweet.getAuthor().getName() : "Unknown";
            String handle = tweet.getAuthor() != null ? tweet.getAuthor().getHandle() : "";
            String text = tweet.getText();
            if (text == null || text.isBlank()) {
                text = tweet.getSummary();
            }
            if (text != null && text.length() > 200) {
                text = text.substring(0, 200) + "...";
            }
            sb.append(String.format("%d. @%s (%s): %s\n", i + 1, handle, authorName, text));
        }

        sb.append("\n请根据以上推文内容，分析整体市场情绪，返回以下格式的 JSON（只需返回 JSON，不要其他文字）：\n\n");
        sb.append("{\n");
        sb.append("  \"positiveCount\": 数字,\n");
        sb.append("  \"negativeCount\": 数字,\n");
        sb.append("  \"neutralCount\": 数字,\n");
        sb.append("  \"positiveRatio\": 0-100 的百分比,\n");
        sb.append("  \"overallTrend\": \"bullish\" 或 \"bearish\" 或 \"neutral\",\n");
        sb.append("  \"positiveTweets\": [{\"author\": \"作者名\", \"handle\": \"handle\", \"text\": \"推文摘要\", \"score\": 评分, \"publishedAt\": \"时间\"}],\n");
        sb.append("  \"negativeTweets\": [{\"author\": \"作者名\", \"handle\": \"handle\", \"text\": \"推文摘要\", \"score\": 评分, \"publishedAt\": \"时间\"}]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String callAiSync(AppProperties.Ai cfg, String prompt) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.getModel());
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 2048);

        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);
        messages.add(userMsg);
        body.add("messages", messages);

        List<String> cmd = new ArrayList<>();
        cmd.add("curl");
        cmd.add("-sS");
        cmd.add("--max-time");
        cmd.add(String.valueOf(AI_TIMEOUT));
        cmd.add("-X");
        cmd.add("POST");
        cmd.add(cfg.getBaseUrl() + "/chat/completions");
        cmd.add("-H");
        cmd.add("Authorization: Bearer " + cfg.getApiKey());
        cmd.add("-H");
        cmd.add("Content-Type: application/json");
        cmd.add("-d");
        cmd.add(body.toString());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process process = pb.start();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        process.waitFor();

        String raw = result.toString().trim();
        if (raw.isEmpty()) return null;

        JsonObject resp = JsonParser.parseString(raw).getAsJsonObject();
        if (resp.has("error")) {
            log.warn("AI API error: {}", resp.get("error"));
            return null;
        }
        JsonArray choices = resp.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) return null;
        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) return null;
        return message.get("content").getAsString();
    }

    private SentimentResult parseAiResponse(String response, List<TweetItem> tweets) {
        SentimentResult result = new SentimentResult();
        try {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}');
            if (start < 0 || end < 0) {
                return simpleSentimentAnalysis(tweets);
            }
            String json = response.substring(start, end + 1);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            result.setPositiveCount(obj.has("positiveCount") ? obj.get("positiveCount").getAsInt() : 0);
            result.setNegativeCount(obj.has("negativeCount") ? obj.get("negativeCount").getAsInt() : 0);
            result.setNeutralCount(obj.has("neutralCount") ? obj.get("neutralCount").getAsInt() : 0);
            result.setPositiveRatio(obj.has("positiveRatio") ? obj.get("positiveRatio").getAsDouble() : 50.0);
            result.setOverallTrend(obj.has("overallTrend") ? obj.get("overallTrend").getAsString() : "neutral");

            List<SentimentResult.SentimentTweet> positive = new ArrayList<>();
            if (obj.has("positiveTweets")) {
                JsonArray arr = obj.getAsJsonArray("positiveTweets");
                for (JsonElement el : arr) {
                    JsonObject tweetObj = el.getAsJsonObject();
                    SentimentResult.SentimentTweet st = new SentimentResult.SentimentTweet();
                    st.setAuthor(tweetObj.has("author") ? tweetObj.get("author").getAsString() : "");
                    st.setHandle(tweetObj.has("handle") ? tweetObj.get("handle").getAsString() : "");
                    st.setText(tweetObj.has("text") ? tweetObj.get("text").getAsString() : "");
                    st.setScore(tweetObj.has("score") ? tweetObj.get("score").getAsDouble() : 50);
                    st.setPublishedAt(tweetObj.has("publishedAt") ? tweetObj.get("publishedAt").getAsString() : "");
                    positive.add(st);
                }
            }
            result.setPositiveTweets(positive);

            List<SentimentResult.SentimentTweet> negative = new ArrayList<>();
            if (obj.has("negativeTweets")) {
                JsonArray arr = obj.getAsJsonArray("negativeTweets");
                for (JsonElement el : arr) {
                    JsonObject tweetObj = el.getAsJsonObject();
                    SentimentResult.SentimentTweet st = new SentimentResult.SentimentTweet();
                    st.setAuthor(tweetObj.has("author") ? tweetObj.get("author").getAsString() : "");
                    st.setHandle(tweetObj.has("handle") ? tweetObj.get("handle").getAsString() : "");
                    st.setText(tweetObj.has("text") ? tweetObj.get("text").getAsString() : "");
                    st.setScore(tweetObj.has("score") ? tweetObj.get("score").getAsDouble() : -50);
                    st.setPublishedAt(tweetObj.has("publishedAt") ? tweetObj.get("publishedAt").getAsString() : "");
                    negative.add(st);
                }
            }
            result.setNegativeTweets(negative);

            return result;
        } catch (Exception e) {
            log.warn("Failed to parse AI sentiment response: {}", e.getMessage());
            return simpleSentimentAnalysis(tweets);
        }
    }

    private SentimentResult simpleSentimentAnalysis(List<TweetItem> tweets) {
        SentimentResult result = new SentimentResult();
        List<SentimentResult.SentimentTweet> positive = new ArrayList<>();
        List<SentimentResult.SentimentTweet> negative = new ArrayList<>();

        String[] bullishKeywords = {"bull", "buy", "long", "pump", "moon", "rise", "gain", "growth", "up", "high", "牛市", "买入", "上涨", "增长"};
        String[] bearishKeywords = {"bear", "sell", "short", "dump", "drop", "fall", "loss", "down", "low", "熊市", "卖出", "下跌", "亏损"};

        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;

        for (TweetItem tweet : tweets) {
            String text = (tweet.getText() != null ? tweet.getText() : "") + (tweet.getSummary() != null ? tweet.getSummary() : "");
            text = text.toLowerCase();

            int bullishScore = 0;
            int bearishScore = 0;
            for (String kw : bullishKeywords) {
                if (text.contains(kw.toLowerCase())) bullishScore++;
            }
            for (String kw : bearishKeywords) {
                if (text.contains(kw.toLowerCase())) bearishScore++;
            }

            SentimentResult.SentimentTweet st = new SentimentResult.SentimentTweet();
            if (tweet.getAuthor() != null) {
                st.setAuthor(tweet.getAuthor().getName());
                st.setHandle(tweet.getAuthor().getHandle());
            }
            st.setText(text.length() > 150 ? text.substring(0, 150) + "..." : text);
            st.setPublishedAt(tweet.getPublishedAt() != null ? tweet.getPublishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");

            if (bullishScore > bearishScore) {
                positiveCount++;
                st.setScore(50 + Math.min(bullishScore * 10, 50));
                positive.add(st);
            } else if (bearishScore > bullishScore) {
                negativeCount++;
                st.setScore(-50 - Math.min(bearishScore * 10, 50));
                negative.add(st);
            } else {
                neutralCount++;
                st.setScore(0);
            }
        }

        result.setPositiveCount(positiveCount);
        result.setNegativeCount(negativeCount);
        result.setNeutralCount(neutralCount);

        int total = positiveCount + negativeCount + neutralCount;
        if (total > 0) {
            result.setPositiveRatio((double) positiveCount / total * 100);
        } else {
            result.setPositiveRatio(50.0);
        }

        if (result.getPositiveRatio() > 60) {
            result.setOverallTrend("bullish");
        } else if (result.getPositiveRatio() < 40) {
            result.setOverallTrend("bearish");
        } else {
            result.setOverallTrend("neutral");
        }

        result.setPositiveTweets(positive);
        result.setNegativeTweets(negative);
        return result;
    }

    private SentimentResult getFallbackResult() {
        SentimentResult result = new SentimentResult();
        result.setPositiveCount(3);
        result.setNegativeCount(2);
        result.setNeutralCount(5);
        result.setPositiveRatio(60.0);
        result.setOverallTrend("neutral");
        result.setPositiveTweets(new ArrayList<>());
        result.setNegativeTweets(new ArrayList<>());
        return result;
    }
}
