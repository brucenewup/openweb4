package com.openweb4.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.TweetAuthor;
import com.openweb4.model.TweetItem;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.openweb4.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class TweetService {

    private static final Logger log = LoggerFactory.getLogger(TweetService.class);
    /** 缓存条数上限（需覆盖配置中的全部大V） */
    private static final int MAX_TWEETS = 120;

    /**
     * 优先展示的大V（创作者/增长类，分页时排在最前）
     */
    private static final List<String> PRIORITY_HANDLE_ORDER = List.of(
            "thejustinwelsh",
            "PaddyG96",
            "rowancheung",
            "thedankoe",
            "bentossell",
            "dickiebush",
            "LinusEkenstam",
            "fortelabs",
            "steveschoger",
            "RyanHoliday"
    );
    private static final int AI_CURL_TIMEOUT = 45;
    /** RSS 单条推文正文入库上限（避免缓存过大） */
    private static final int RSS_BODY_MAX_CHARS = 800;
    /** 列表摘要行长度 */
    private static final int RSS_SUMMARY_MAX_CHARS = 180;

    private static final List<String> RSS_BASES = List.of(
            "https://rsshub.app/twitter/user/",
            "https://nitter.poast.org/",
            "https://nitter.privacydev.net/"
    );

    // 真实大V信息映射表
    private static final Map<String, String> HANDLE_DISPLAY_NAMES = Map.ofEntries(
            Map.entry("elonmusk",          "Elon Musk"),
            Map.entry("cz_binance",        "CZ BNB"),
            Map.entry("VitalikButerin",    "Vitalik Buterin"),
            Map.entry("CryptoHayes",       "Arthur Hayes"),
            Map.entry("saylor",            "Michael Saylor"),
            Map.entry("BarrySilbert",      "Barry Silbert"),
            Map.entry("APompliano",        "Anthony Pompliano"),
            Map.entry("PeterSchiff",       "Peter Schiff"),
            Map.entry("TimFerriss",        "Tim Ferriss"),
            Map.entry("naval",             "Naval Ravikant"),
            Map.entry("TheBlock__",        "The Block"),
            Map.entry("fundstrat",         "Fundstrat"),
            Map.entry("woonomic",          "Willy Woo"),
            Map.entry("scottmelker",       "Scott Melker"),
            Map.entry("MichaelvanDePoppe", "Michaël van de Poppe"),
            Map.entry("CryptoCobain",      "Crypto Cobain"),
            Map.entry("Bitboy_Crypto",     "BitBoy Crypto"),
            Map.entry("CryptoRus",         "CryptoRus"),
            Map.entry("thejustinwelsh",    "Justin Welsh"),
            Map.entry("PaddyG96",          "Paddy Galloway"),
            Map.entry("rowancheung",       "Rowan Cheung"),
            Map.entry("thedankoe",         "Dan Koe"),
            Map.entry("bentossell",        "Ben Tossell"),
            Map.entry("dickiebush",        "Dickie Bush"),
            Map.entry("LinusEkenstam",     "Linus Ekenstam"),
            Map.entry("fortelabs",         "Forte Labs"),
            Map.entry("steveschoger",      "Steve Schoger"),
            Map.entry("RyanHoliday",       "Ryan Holiday")
    );

    // 大V简介（用于 AI 生成时的背景知识）
    private static final Map<String, String> HANDLE_BIO = Map.ofEntries(
            Map.entry("elonmusk",          "Tesla/SpaceX CEO，比特币和狗狗币支持者"),
            Map.entry("cz_binance",        "币安创始人，全球最大交易所"),
            Map.entry("VitalikButerin",    "以太坊创始人，区块链技术领袖"),
            Map.entry("CryptoHayes",       "BitMEX联创，宏观加密分析师"),
            Map.entry("saylor",            "MicroStrategy CEO，比特币最大机构持有者"),
            Map.entry("BarrySilbert",      "DCG集团创始人，加密投资人"),
            Map.entry("APompliano",        "Morgan Creek Digital联创，加密布道者"),
            Map.entry("PeterSchiff",       "黄金多头，比特币怀疑论者"),
            Map.entry("TimFerriss",        "《4小时工作制》作者，早期加密投资者"),
            Map.entry("naval",             "AngelList创始人，哲学家投资人"),
            Map.entry("TheBlock__",        "加密行业权威媒体"),
            Map.entry("fundstrat",         "Tom Lee领导的加密研究机构"),
            Map.entry("woonomic",          "链上数据分析师Willy Woo"),
            Map.entry("scottmelker",       "The Wolf Of All Streets，加密交易员"),
            Map.entry("MichaelvanDePoppe", "荷兰加密交易员，山寨币分析师"),
            Map.entry("CryptoCobain",      "匿名加密KOL，DeFi早期参与者"),
            Map.entry("Bitboy_Crypto",     "最大加密YouTube频道之一"),
            Map.entry("CryptoRus",         "加密社区知名博主"),
            Map.entry("thejustinwelsh",    "一人公司、独立创业者与副业体系"),
            Map.entry("PaddyG96",          "YouTube 算法、增长与内容策略"),
            Map.entry("rowancheung",       "AI 工具与前沿科技资讯"),
            Map.entry("thedankoe",         "个人品牌、内容创作与变现"),
            Map.entry("bentossell",        "无代码工具与自动化工作流"),
            Map.entry("dickiebush",        "数字写作、受众增长与内容产品"),
            Map.entry("LinusEkenstam",     "AI 视觉、图像生成与创意工具"),
            Map.entry("fortelabs",         "知识管理、第二大脑与生产力系统"),
            Map.entry("steveschoger",      "实用 UI/UX 与界面设计"),
            Map.entry("RyanHoliday",       "斯多葛哲学、心智成长与写作")
    );

    private final List<TweetItem> cachedTweets = new CopyOnWriteArrayList<>();
    /** 与缓存写入、冷启动引导共用，避免并发 clear/add 交错 */
    private final Object tweetCacheLock = new Object();
    /** 冷缓存时只调度一次后台 RSS/AI 预热，避免重复风暴 */
    private volatile boolean tweetWarmupPending;
    private final AppProperties appProperties;
    private final OkHttpClient http;

    public TweetService(AppProperties appProperties, OkHttpClient http) {
        this.appProperties = appProperties;
        this.http = http;
    }

    public List<TweetItem> fetchLatestTweets() {
        // 1. 先尝试 RSS 抓取
        List<TweetItem> tweets = fetchFromFeeds();

        // 2. RSS 失败则调用 AI 生成
        if (tweets.isEmpty()) {
            log.info("RSS feeds unavailable, falling back to AI-generated KOL updates");
            tweets = fetchFromAI();
        }

        // 3. AI 也失败则展示大V信息卡片
        if (tweets.isEmpty()) {
            log.warn("AI generation failed, using static KOL profile cards");
            tweets = buildProfileCards();
        }

        sortTweetsPriorityThenTime(tweets);
        if (tweets.size() > MAX_TWEETS) {
            tweets = new ArrayList<>(tweets.subList(0, MAX_TWEETS));
        }
        synchronized (tweetCacheLock) {
            cachedTweets.clear();
            cachedTweets.addAll(tweets);
        }
        return List.copyOf(tweets);
    }

    /**
     * 优先账号（PRIORITY_HANDLE_ORDER）排前，同组内按发布时间倒序。
     */
    private static void sortTweetsPriorityThenTime(List<TweetItem> tweets) {
        tweets.sort(Comparator
                .comparingInt((TweetItem t) -> priorityRank(t.getAuthor().getHandle()))
                .thenComparing(TweetItem::getPublishedAt, Comparator.reverseOrder()));
    }

    private static int priorityRank(String handle) {
        if (handle == null) return 10_000;
        int idx = PRIORITY_HANDLE_ORDER.indexOf(handle);
        return idx >= 0 ? idx : 10_000;
    }

    public List<TweetItem> getCachedTweets() {
        if (!cachedTweets.isEmpty()) {
            return List.copyOf(cachedTweets);
        }
        /*
         * 冷启动：完整 RSS 轮询可能长达数分钟，同步执行会导致浏览器/网关超时，
         * spa.html 的 fetchJson 失败即显示「加载失败」。先快速返回静态大V卡片并后台预热缓存。
         */
        synchronized (tweetCacheLock) {
            if (!cachedTweets.isEmpty()) {
                return List.copyOf(cachedTweets);
            }
            List<TweetItem> bootstrap = buildProfileCards();
            cachedTweets.clear();
            cachedTweets.addAll(bootstrap);
            if (!tweetWarmupPending) {
                tweetWarmupPending = true;
                CompletableFuture.runAsync(() -> {
                    try {
                        fetchLatestTweets();
                    } catch (Exception e) {
                        log.warn("Background KOL tweet warmup failed: {}", e.getMessage(), e);
                    } finally {
                        synchronized (tweetCacheLock) {
                            tweetWarmupPending = false;
                        }
                    }
                });
            }
            return List.copyOf(cachedTweets);
        }
    }

    public List<TweetItem> getLatestTweets(boolean forceRefresh, Locale locale) {
        return forceRefresh ? fetchLatestTweets() : getCachedTweets();
    }

    public List<TweetAuthor> getAllAuthors() {
        List<TweetAuthor> authors = new ArrayList<>();
        for (String handle : appProperties.getTweets().getHandles()) {
            if (handle == null || handle.isBlank()) continue;
            String h = handle.trim().replace("@", "");
            authors.add(new TweetAuthor(displayName(h), h, "https://x.com/" + h));
        }
        return authors;
    }

    // ─── RSS 抓取 ───────────────────────────────────────────────────────────────

    private List<TweetItem> fetchFromFeeds() {
        List<TweetItem> tweets = new ArrayList<>();
        for (TweetAuthor author : configuredAuthors()) {
            TweetItem item = fetchLatestForAuthor(author);
            if (item != null) tweets.add(item);
        }
        return tweets;
    }

    private TweetItem fetchLatestForAuthor(TweetAuthor author) {
        for (String base : RSS_BASES) {
            TweetItem item = tryFeed(base, author);
            if (item != null) return item;
        }
        return null;
    }

    private TweetItem tryFeed(String base, TweetAuthor author) {
        String url = base.contains("rsshub.app") ? base + author.getHandle() : base + author.getHandle() + "/rss";
        try {
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OpenWeb4Bot/1.0)")
                    .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                    .get().build();
            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return null;
                byte[] bytes = resp.body().bytes();
                SyndFeed feed;
                try (XmlReader reader = new XmlReader(new ByteArrayInputStream(bytes))) {
                    feed = new SyndFeedInput().build(reader);
                }
                if (feed.getEntries() == null || feed.getEntries().isEmpty()) return null;
                SyndEntry entry = feed.getEntries().get(0);
                String link = entry.getLink() != null ? entry.getLink() : author.getProfileUrl();
                String rawBody = bestFeedEntryBody(entry);
                if (rawBody.isBlank()) return null;
                String text = clipUtf16(rawBody, RSS_BODY_MAX_CHARS);
                String summary = clipUtf16(rawBody, RSS_SUMMARY_MAX_CHARS);
                LocalDateTime publishedAt = entry.getPublishedDate() != null
                        ? LocalDateTime.ofInstant(entry.getPublishedDate().toInstant(), ZoneId.systemDefault())
                        : LocalDateTime.now();
                String id = entry.getUri() != null ? entry.getUri()
                        : author.getHandle() + "-" + publishedAt.atZone(ZoneId.systemDefault()).toEpochSecond();
                return new TweetItem(id, text, summary, normalizeTweetUrl(link, author), publishedAt,
                        base.contains("rsshub") ? "RSSHub" : "Nitter RSS", LocalDateTime.now(), author);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    // ─── AI 生成大V动态 ─────────────────────────────────────────────────────────

    private List<TweetItem> fetchFromAI() {
        AppProperties.Ai cfg = appProperties.getAi();
        if (cfg.getApiKey() == null || cfg.getApiKey().trim().isEmpty()) {
            log.warn("AI_API_KEY not configured, skipping AI tweet generation");
            return Collections.emptyList();
        }

        List<TweetAuthor> authors = configuredAuthors();
        // 构造大V列表描述
        StringBuilder authorList = new StringBuilder();
        for (TweetAuthor a : authors) {
            String bio = HANDLE_BIO.getOrDefault(a.getHandle(), "加密行业KOL");
            authorList.append("- @").append(a.getHandle()).append("（").append(a.getName()).append("，").append(bio).append("）\n");
        }

        String prompt = "你是一个加密货币行业信息助手。请根据以下加密行业知名KOL列表，" +
                "结合他们各自的风格和立场，为每位KOL生成一条符合其风格的最新推文内容（英文原文）和中文摘要。\n\n" +
                "KOL列表：\n" + authorList +
                "\n请严格按照以下JSON格式返回，不要有任何额外文字：\n" +
                "{\"tweets\":[{\"handle\":\"xxx\",\"text\":\"英文推文内容\",\"summary\":\"中文摘要\"},...]}";

        try {
            String responseBody = callAiSync(cfg, prompt);
            if (responseBody == null || responseBody.isBlank()) return Collections.emptyList();

            // 提取 JSON
            int start = responseBody.indexOf('{');
            int end = responseBody.lastIndexOf('}');
            if (start < 0 || end < 0) return Collections.emptyList();
            String json = responseBody.substring(start, end + 1);

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray tweetsArr = root.getAsJsonArray("tweets");
            if (tweetsArr == null) return Collections.emptyList();

            List<TweetItem> result = new ArrayList<>();
            // 构建 handle -> author 映射
            Map<String, TweetAuthor> authorMap = new HashMap<>();
            for (TweetAuthor a : authors) authorMap.put(a.getHandle(), a);

            for (int i = 0; i < tweetsArr.size(); i++) {
                JsonObject obj = tweetsArr.get(i).getAsJsonObject();
                String handle = obj.has("handle") ? obj.get("handle").getAsString().replace("@", "") : null;
                String text = obj.has("text") ? obj.get("text").getAsString() : "";
                String summary = obj.has("summary") ? obj.get("summary").getAsString() : text;
                if (handle == null || text.isBlank()) continue;

                TweetAuthor author = authorMap.getOrDefault(handle,
                        new TweetAuthor(displayName(handle), handle, "https://x.com/" + handle));
                LocalDateTime publishedAt = LocalDateTime.now().minusMinutes(i * 8L);
                result.add(new TweetItem(
                        "ai-" + handle + "-" + System.currentTimeMillis(),
                        text, summary,
                        "https://x.com/" + handle,
                        publishedAt, "AI Generated", LocalDateTime.now(), author
                ));
            }
            sortTweetsPriorityThenTime(result);
            log.info("AI generated {} KOL tweets", result.size());
            return result;
        } catch (Exception e) {
            log.warn("AI tweet generation failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
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
        cmd.add("curl"); cmd.add("-sS");
        cmd.add("--max-time"); cmd.add(String.valueOf(AI_CURL_TIMEOUT));
        cmd.add("-X"); cmd.add("POST");
        cmd.add(cfg.getBaseUrl() + "/chat/completions");
        cmd.add("-H"); cmd.add("Authorization: Bearer " + cfg.getApiKey());
        cmd.add("-H"); cmd.add("Content-Type: application/json");
        cmd.add("-d"); cmd.add(body.toString());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process process = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        process.waitFor();

        String raw = sb.toString().trim();
        if (raw.isEmpty()) return null;

        // 解析非流式响应
        JsonObject resp = JsonParser.parseString(raw).getAsJsonObject();
        // 检查 API 错误
        if (resp.has("error") || resp.has("code")) {
            String errMsg = resp.has("message") ? resp.get("message").getAsString() : raw;
            log.warn("AI API error: {}", errMsg);
            return null;
        }
        JsonArray choices = resp.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) return null;
        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null) return null;
        return message.get("content").getAsString();
    }

    // ─── 静态大V信息卡片（最终兜底）────────────────────────────────────────────

    private List<TweetItem> buildProfileCards() {
        List<TweetItem> tweets = new ArrayList<>();
        List<TweetAuthor> authors = new ArrayList<>(configuredAuthors());
        authors.sort(Comparator.comparingInt(a -> priorityRank(a.getHandle())));
        for (int i = 0; i < authors.size(); i++) {
            TweetAuthor a = authors.get(i);
            String bio = HANDLE_BIO.getOrDefault(a.getHandle(), "加密行业 KOL");
            // TweetItem(text, summary)：text=最近一条展示正文；summary=简介。此前参数颠倒导致界面错乱。
            String noFeedMsg = "暂未能通过 RSS 抓取到 @" + a.getHandle()
                    + " 的最新推文，请点击卡片前往 X 查看其主页与最新动态。";
            tweets.add(new TweetItem(
                    "profile-" + a.getHandle(),
                    noFeedMsg,
                    bio,
                    "https://x.com/" + a.getHandle(),
                    LocalDateTime.now().minusMinutes(i * 5L),
                    "Profile",
                    LocalDateTime.now(),
                    a
            ));
        }
        return tweets;
    }

    // ─── 工具方法 ───────────────────────────────────────────────────────────────

    private List<TweetAuthor> configuredAuthors() {
        String[] handles = appProperties.getTweets().getHandles();
        if (handles == null || handles.length == 0) {
            handles = new String[]{"elonmusk", "cz_binance", "VitalikButerin", "CryptoHayes", "saylor"};
        }
        List<TweetAuthor> authors = new ArrayList<>();
        for (String raw : handles) {
            if (raw == null || raw.isBlank()) continue;
            String handle = raw.trim().replace("@", "");
            authors.add(new TweetAuthor(displayName(handle), handle, "https://x.com/" + handle));
        }
        return authors;
    }

    private String displayName(String handle) {
        return HANDLE_DISPLAY_NAMES.getOrDefault(handle, handle);
    }

    private String normalizeTweetUrl(String link, TweetAuthor author) {
        if (link == null || link.isBlank()) return author.getProfileUrl();
        if (link.contains("nitter.")) return link.replaceFirst("https?://[^/]+/", "https://x.com/");
        return link;
    }

    private String cleanText(String value) {
        if (value == null) return "";
        return HtmlUtils.htmlUnescape(value.replaceAll("<[^>]+>", " ")).replaceAll("\\s+", " ").trim();
    }

    /**
     * 从 RSS 条目中取最长的一段作为推文正文（优先 content:encoded / SyndContent，其次 description，最后 title）。
     */
    private String bestFeedEntryBody(SyndEntry entry) {
        String best = "";
        if (entry.getContents() != null) {
            for (SyndContent c : entry.getContents()) {
                if (c == null || c.getValue() == null) continue;
                String v = cleanText(c.getValue());
                if (v.length() > best.length()) best = v;
            }
        }
        if (entry.getDescription() != null) {
            String d = cleanText(entry.getDescription().getValue());
            if (d.length() > best.length()) best = d;
        }
        String title = cleanText(entry.getTitle());
        if (best.isEmpty()) return title;
        return best;
    }

    private static String clipUtf16(String s, int maxChars) {
        if (s == null || s.isEmpty()) return "";
        if (s.length() <= maxChars) return s;
        if (maxChars <= 1) return "…";
        return s.substring(0, maxChars - 1) + "…";
    }
}
