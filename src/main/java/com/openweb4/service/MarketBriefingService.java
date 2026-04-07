package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openweb4.model.CryptoPrice;
import com.openweb4.model.NewsArticle;
import com.openweb4.model.TweetItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MarketBriefingService {

    private static final Logger log = LoggerFactory.getLogger(MarketBriefingService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AiChatService aiChatService;
    private final CryptoPriceService cryptoPriceService;
    private final NewsService newsService;
    private final TweetService tweetService;
    private final Cache<String, BriefingData> cache;

    public MarketBriefingService(AiChatService aiChatService,
                                 CryptoPriceService cryptoPriceService,
                                 NewsService newsService,
                                 TweetService tweetService) {
        this.aiChatService = aiChatService;
        this.cryptoPriceService = cryptoPriceService;
        this.newsService = newsService;
        this.tweetService = tweetService;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    public BriefingData getLatestBriefing() {
        String cacheKey = "daily_briefing";
        BriefingData cached = cache.getIfPresent(cacheKey);
        
        if (cached != null) {
            log.debug("Returning cached market briefing from {}", cached.generatedAt);
            cached.cached = true;
            return cached;
        }

        log.info("No cached briefing found, generating new one");
        return generateBriefing();
    }

    public BriefingData generateBriefing() {
        log.info("Generating market briefing");
        
        try {
            // Gather market data
            CryptoPrice btcPrice = cryptoPriceService.getBitcoinPrice();
            CryptoPrice ethPrice = cryptoPriceService.getEthereumPrice();
            List<NewsArticle> news = newsService.getCachedNews();
            List<TweetItem> tweets = tweetService.getCachedTweets();

            // Build prompt for AI
            StringBuilder prompt = new StringBuilder();
            prompt.append("请生成今日加密货币市场简报，包含以下内容：\n\n");
            
            prompt.append("## 市场行情\n");
            if (btcPrice != null) {
                prompt.append(String.format("- BTC: $%,.2f (24h变化: %.2f%%)\n", 
                        btcPrice.getPrice(), btcPrice.getChange24h()));
            }
            if (ethPrice != null) {
                prompt.append(String.format("- ETH: $%,.2f (24h变化: %.2f%%)\n", 
                        ethPrice.getPrice(), ethPrice.getChange24h()));
            }
            
            prompt.append("\n## 最新新闻\n");
            if (news != null && !news.isEmpty()) {
                int count = Math.min(5, news.size());
                for (int i = 0; i < count; i++) {
                    NewsArticle item = news.get(i);
                    prompt.append(String.format("- %s\n", item.getTitle()));
                }
            } else {
                prompt.append("- 暂无最新新闻\n");
            }
            
            prompt.append("\n## KOL 观点\n");
            if (tweets != null && !tweets.isEmpty()) {
                int count = Math.min(5, tweets.size());
                for (int i = 0; i < count; i++) {
                    TweetItem tweet = tweets.get(i);
                    prompt.append(String.format("- @%s: %s\n", 
                            tweet.getAuthor(), 
                            tweet.getText().length() > 100 ? 
                                    tweet.getText().substring(0, 100) + "..." : 
                                    tweet.getText()));
                }
            } else {
                prompt.append("- 暂无 KOL 观点\n");
            }
            
            prompt.append("\n请用简洁专业的语言总结以上信息，生成一份200-300字的市场简报。");

            // Call AI service
            AtomicReference<String> briefingContent = new AtomicReference<>("");
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            aiChatService.streamReply(prompt.toString(), chunk -> {
                briefingContent.updateAndGet(current -> current + chunk);
            });
            
            // Wait a bit for AI response (non-blocking in real scenario)
            Thread.sleep(2000);
            
            String content = briefingContent.get();
            if (content.isEmpty() || content.startsWith("[错误]")) {
                content = generateFallbackBriefing(btcPrice, ethPrice, news, tweets);
            }

            BriefingData briefing = new BriefingData();
            briefing.content = content;
            briefing.generatedAt = LocalDateTime.now().format(FORMATTER);
            briefing.cached = false;

            cache.put("daily_briefing", briefing);
            log.info("Market briefing generated successfully");
            
            return briefing;
            
        } catch (Exception e) {
            log.error("Failed to generate market briefing", e);
            
            BriefingData fallback = new BriefingData();
            fallback.content = "市场简报生成失败，请稍后再试。";
            fallback.generatedAt = LocalDateTime.now().format(FORMATTER);
            fallback.cached = false;
            return fallback;
        }
    }

    private String generateFallbackBriefing(CryptoPrice btcPrice, CryptoPrice ethPrice, 
                                           List<NewsArticle> news, List<TweetItem> tweets) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 今日市场简报\n\n");
        
        sb.append("市场行情：\n");
        if (btcPrice != null) {
            sb.append(String.format("BTC 当前价格 $%,.2f，24小时变化 %.2f%%。", 
                    btcPrice.getPrice(), btcPrice.getChange24h()));
        }
        if (ethPrice != null) {
            sb.append(String.format("ETH 当前价格 $%,.2f，24小时变化 %.2f%%。", 
                    ethPrice.getPrice(), ethPrice.getChange24h()));
        }
        
        sb.append("\n\n");
        
        if (news != null && !news.isEmpty()) {
            sb.append("市场动态：今日共有 ").append(news.size()).append(" 条加密货币相关新闻。");
        }
        
        if (tweets != null && !tweets.isEmpty()) {
            sb.append("KOL 观点活跃，共 ").append(tweets.size()).append(" 条最新推文。");
        }
        
        return sb.toString();
    }

    public static class BriefingData {
        public String content;
        public String generatedAt;
        public boolean cached;
    }
}
