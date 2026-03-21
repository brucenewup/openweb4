package com.openweb4.service;

import com.openweb4.model.NewsArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsService.class);
    private static final int MAX_SUMMARY_LENGTH = 200;
    private static final int MAX_ARTICLES = 30;
    private static final int TRANSLATE_MAX_CHARS = 240;

    private final List<NewsArticle> cachedNews = new CopyOnWriteArrayList<>();
    private final OkHttpClient http;

    public NewsService(OkHttpClient http) {
        this.http = http;
    }
    private final Cache<String, String> zhTranslateCache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public List<NewsArticle> fetchCryptoNews() {
        List<NewsArticle> articles = new ArrayList<>();
        fetchRssFeed("https://www.coindesk.com/arc/outboundfeeds/rss/", "CoinDesk", articles);
        fetchRssFeed("https://cointelegraph.com/rss", "CoinTelegraph", articles);

        if (articles.isEmpty()) {
            articles.addAll(buildFallbackArticles());
        }

        articles.sort(Comparator.comparing(NewsArticle::getPublishedAt).reversed());
        if (articles.size() > MAX_ARTICLES) {
            articles = new ArrayList<>(articles.subList(0, MAX_ARTICLES));
        }

        cachedNews.clear();
        cachedNews.addAll(articles);
        return List.copyOf(articles);
    }

    private void fetchRssFeed(String feedUrl, String source, List<NewsArticle> articles) {
        try {
            Request req = new Request.Builder()
                    .url(feedUrl)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OpenWeb4Bot/1.0; +https://openweb4.io)")
                    .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                    .get()
                    .build();
            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    log.warn("Failed to fetch RSS from {}: HTTP {}", source, resp.code());
                    return;
                }
                byte[] bytes = resp.body().bytes();
                SyndFeedInput input = new SyndFeedInput();
                try (XmlReader reader = new XmlReader(new java.io.ByteArrayInputStream(bytes))) {
                    SyndFeed feed = input.build(reader);
                    for (SyndEntry entry : feed.getEntries()) {
                        String title = cleanText(entry.getTitle());
                        String summary = entry.getDescription() != null
                                ? cleanText(entry.getDescription().getValue())
                                : "";
                        if (summary.length() > MAX_SUMMARY_LENGTH) {
                            summary = summary.substring(0, MAX_SUMMARY_LENGTH) + "...";
                        }
                        LocalDateTime publishedAt = LocalDateTime.now();
                        if (entry.getPublishedDate() != null) {
                            publishedAt = LocalDateTime.ofInstant(
                                    entry.getPublishedDate().toInstant(), ZoneId.systemDefault());
                        }
                        articles.add(new NewsArticle(title, summary, source, entry.getLink(), publishedAt));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch RSS from {}: {}", source, e.getMessage());
        }
    }

    public List<NewsArticle> getCachedNews() {
        if (cachedNews.isEmpty()) {
            return fetchCryptoNews();
        }
        return List.copyOf(cachedNews);
    }

    public List<NewsArticle> getNews(boolean forceRefresh, Locale locale) {
        List<NewsArticle> base = forceRefresh ? fetchCryptoNews() : getCachedNews();
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return translateToZh(base);
        }
        return base;
    }

    private List<NewsArticle> buildFallbackArticles() {
        List<NewsArticle> fallback = new ArrayList<>();
        fallback.add(new NewsArticle(
                "Bitcoin Surges Past Key Resistance Level",
                "Bitcoin has broken through a major resistance level as institutional buying continues to increase.",
                "CryptoNews", "https://example.com/btc-surge", LocalDateTime.now().minusHours(1)));
        fallback.add(new NewsArticle(
                "Ethereum 2.0 Staking Reaches New Milestone",
                "The amount of ETH staked in the Beacon Chain has reached a new all-time high.",
                "CryptoNews", "https://example.com/eth-staking", LocalDateTime.now().minusHours(2)));
        fallback.add(new NewsArticle(
                "USDT Market Cap Hits Record $100 Billion",
                "Tether's market capitalization has reached a historic $100 billion as stablecoin adoption grows.",
                "CryptoNews", "https://example.com/usdt-record", LocalDateTime.now().minusHours(3)));
        return fallback;
    }

    private String cleanText(String value) {
        if (value == null) return "";
        String withoutHtml = value.replaceAll("<[^>]+>", " ");
        return HtmlUtils.htmlUnescape(withoutHtml).replaceAll("\\s+", " ").trim();
    }

    private List<NewsArticle> translateToZh(List<NewsArticle> articles) {
        List<NewsArticle> out = new ArrayList<>(articles.size());
        for (NewsArticle a : articles) {
            String title = translateEnToZh(a.getTitle());
            String summary = translateEnToZh(a.getSummary());
            out.add(new NewsArticle(title, summary, a.getSource(), a.getUrl(), a.getPublishedAt()));
        }
        return out;
    }

    private String translateEnToZh(String text) {
        if (text == null) return "";
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return "";
        if (containsCjk(trimmed)) return trimmed;
        String cacheKey = trimmed.length() > TRANSLATE_MAX_CHARS ? trimmed.substring(0, TRANSLATE_MAX_CHARS) : trimmed;
        String cached = zhTranslateCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        String toTranslate = cacheKey;
        try {
            // Unofficial but commonly available endpoint; failures will fallback to original text.
            HttpUrl url = HttpUrl.parse("https://translate.googleapis.com/translate_a/single")
                    .newBuilder()
                    .addQueryParameter("client", "gtx")
                    .addQueryParameter("sl", "auto")
                    .addQueryParameter("tl", "zh-CN")
                    .addQueryParameter("dt", "t")
                    .addQueryParameter("q", toTranslate)
                    .build();

            Request req = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .build();

            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    return trimmed;
                }
                String body = resp.body().string();
                String translated = extractGoogleTranslateText(body);
                if (translated == null || translated.trim().isEmpty()) {
                    return trimmed;
                }
                String finalText = translated.trim();
                zhTranslateCache.put(cacheKey, finalText);
                return finalText;
            }
        } catch (Exception e) {
            return trimmed;
        }
    }

    private static boolean containsCjk(String s) {
        for (int i = 0; i < s.length(); i++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(s.charAt(i));
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                    || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Best-effort extraction from Google translate array format.
     * Example: [[[\"你好\",\"hello\",null,null,1]],null,\"en\",...]
     */
    private static String extractGoogleTranslateText(String raw) {
        if (raw == null) return null;
        int firstQuote = raw.indexOf("\"");
        if (firstQuote < 0) return null;
        int secondQuote = raw.indexOf("\"", firstQuote + 1);
        if (secondQuote < 0) return null;
        String chunk = raw.substring(firstQuote + 1, secondQuote);
        return chunk.replace("\\n", "\n").replace("\\\"", "\"");
    }
}
