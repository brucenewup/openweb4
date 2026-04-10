package com.openweb4.controller;

import com.openweb4.model.CryptoPrice;
import com.openweb4.model.ExchangeCapability;
import com.openweb4.model.HotCoin;
import com.openweb4.model.MarketCapItem;
import com.openweb4.model.NewsArticle;
import com.openweb4.model.SentimentResult;
import com.openweb4.model.TweetAuthor;
import com.openweb4.model.TweetItem;
import com.openweb4.model.WhaleTransaction;
import com.openweb4.service.CryptoPriceService;
import com.openweb4.service.ExchangeCapabilityService;
import com.openweb4.service.HotCoinService;
import com.openweb4.service.MarketCapHeatmapService;
import com.openweb4.service.NewsService;
import com.openweb4.service.SentimentAnalysisService;
import com.openweb4.service.TransactionSkillService;
import com.openweb4.service.WhaleTrackingService;
import com.openweb4.service.TweetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@RestController
public class ApiController {

    private final CryptoPriceService cryptoPriceService;
    private final WhaleTrackingService whaleTrackingService;
    private final NewsService newsService;
    private final TweetService tweetService;
    private final TransactionSkillService transactionSkillService;
    private final HotCoinService hotCoinService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final MarketCapHeatmapService marketCapHeatmapService;
    private final ExchangeCapabilityService exchangeCapabilityService;

    public ApiController(CryptoPriceService cryptoPriceService,
                         WhaleTrackingService whaleTrackingService,
                         NewsService newsService,
                         TweetService tweetService,
                         TransactionSkillService transactionSkillService,
                         HotCoinService hotCoinService,
                         SentimentAnalysisService sentimentAnalysisService,
                         MarketCapHeatmapService marketCapHeatmapService,
                         ExchangeCapabilityService exchangeCapabilityService) {
        this.cryptoPriceService = cryptoPriceService;
        this.whaleTrackingService = whaleTrackingService;
        this.newsService = newsService;
        this.tweetService = tweetService;
        this.transactionSkillService = transactionSkillService;
        this.hotCoinService = hotCoinService;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.marketCapHeatmapService = marketCapHeatmapService;
        this.exchangeCapabilityService = exchangeCapabilityService;
    }

    @GetMapping("/api/overview")
    public Map<String, Object> overview() {
        CryptoPrice btc = cryptoPriceService.getBitcoinPrice();
        CryptoPrice eth = cryptoPriceService.getEthereumPrice();
        CryptoPrice usdt = cryptoPriceService.getTetherPrice();
        List<WhaleTransaction> transactions = whaleTrackingService.getRecentWhaleTransactions();
        List<WhaleTransaction> flowData = whaleTrackingService.getHistoricalFlowData();

        return Map.of(
                "btc", btc,
                "eth", eth,
                "usdt", usdt,
                "transactions", transactions,
                "flowData", flowData
        );
    }

    @GetMapping("/api/price")
    public Map<String, Object> price(
            @RequestParam(name = "symbol", defaultValue = "BTC") String symbol) {
        CryptoPrice p = cryptoPriceService.getPriceBySymbol(symbol);
        List<WhaleTransaction> transactions = whaleTrackingService.getRecentWhaleTransactions();
        List<WhaleTransaction> flowData = whaleTrackingService.getHistoricalFlowData();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("symbol", symbol.toUpperCase());
        result.put("price", p);
        result.put("transactions", transactions);
        result.put("flowData", flowData);
        return result;
    }

    @GetMapping("/api/news")
    public Map<String, Object> news(@RequestParam(name = "refresh", required = false, defaultValue = "0") int refresh,
                                    @RequestParam(name = "lang", required = false, defaultValue = "") String lang,
                                    @RequestParam(name = "page", required = false) Integer page,
                                    @RequestParam(name = "size", required = false) Integer size,
                                    Locale locale) {
        boolean forceRefresh = refresh == 1;
        Locale effectiveLocale = resolveLocale(lang, locale);
        List<NewsArticle> all = newsService.getNews(forceRefresh, effectiveLocale);
        return paginatedBody(all, "articles", page, size);
    }

    @GetMapping("/api/tweets/latest")
    public Map<String, Object> latestTweets(@RequestParam(name = "refresh", required = false, defaultValue = "0") int refresh,
                                            @RequestParam(name = "lang", required = false, defaultValue = "") String lang,
                                            @RequestParam(name = "page", required = false) Integer page,
                                            @RequestParam(name = "size", required = false) Integer size,
                                            Locale locale) {
        boolean forceRefresh = refresh == 1;
        Locale effectiveLocale = resolveLocale(lang, locale);
        List<TweetItem> all = tweetService.getLatestTweets(forceRefresh, effectiveLocale);
        return paginatedBody(all, "tweets", page, size);
    }

    /**
     * 与 KOL 推文一致：不传 page/size 时返回全量列表（兼容旧客户端）；传任一则分页，size 限制 1–50。
     */
    private static <T> Map<String, Object> paginatedBody(List<T> all, String listKey, Integer page, Integer size) {
        int total = all.size();
        boolean paginate = page != null || size != null;
        List<T> slice;
        int safePage;
        int safeSize;
        int totalPages;

        if (!paginate) {
            slice = all;
            safePage = 0;
            safeSize = total;
            totalPages = total == 0 ? 0 : 1;
        } else {
            safeSize = Math.min(Math.max(size != null ? size : 10, 1), 50);
            totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
            safePage = Math.max(page != null ? page : 0, 0);
            if (totalPages > 0 && safePage >= totalPages) {
                safePage = totalPages - 1;
            }
            int from = safePage * safeSize;
            int to = Math.min(from + safeSize, total);
            slice = from >= total ? Collections.emptyList() : all.subList(from, to);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(listKey, slice);
        body.put("page", safePage);
        body.put("size", safeSize);
        body.put("total", total);
        body.put("totalPages", totalPages);
        return body;
    }

    private Locale resolveLocale(String langParam, Locale fallback) {
        if (langParam != null && !langParam.isBlank()) {
            if (langParam.startsWith("zh")) return Locale.SIMPLIFIED_CHINESE;
            if (langParam.startsWith("en")) return Locale.ENGLISH;
        }
        return fallback != null ? fallback : Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 返回所有已配置的大V列表，供前端 KOL 订阅管理使用
     */
    @GetMapping("/api/kol")
    public Map<String, Object> kolAuthors() {
        List<TweetAuthor> authors = tweetService.getAllAuthors();
        return Map.of("authors", authors);
    }

    @GetMapping("/api/transaction-skills")
    public Map<String, Object> transactionSkills(@RequestParam(name = "lang", required = false, defaultValue = "") String lang,
                                                 Locale locale) {
        Locale effectiveLocale = resolveLocale(lang, locale);
        return transactionSkillService.getComparison(effectiveLocale);
    }

    // P2-7: 山寨币热度雷达
    @GetMapping("/api/hot-coins")
    public Map<String, Object> hotCoins() {
        List<HotCoin> coins = hotCoinService.getTrendingCoins();
        return Map.of("coins", coins);
    }

    // P2-8: KOL情绪分析
    @GetMapping("/api/sentiment")
    public Map<String, Object> sentiment(@RequestParam(name = "refresh", required = false, defaultValue = "0") int refresh) {
        SentimentResult result = sentimentAnalysisService.analyze(refresh == 1);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("positiveCount", result.getPositiveCount());
        body.put("negativeCount", result.getNegativeCount());
        body.put("neutralCount", result.getNeutralCount());
        body.put("positiveRatio", result.getPositiveRatio());
        body.put("overallTrend", result.getOverallTrend());
        body.put("positiveTweets", result.getPositiveTweets());
        body.put("negativeTweets", result.getNegativeTweets());
        return body;
    }

    // P2-9: 加密市值热力图
    @GetMapping("/api/heatmap")
    public Map<String, Object> heatmap() {
        List<MarketCapItem> items = marketCapHeatmapService.getHeatmapData();
        return Map.of("items", items);
    }

    // P2-10: 交易所能力对比卡片
    @GetMapping("/api/exchange-capabilities")
    public Map<String, Object> exchangeCapabilities(@RequestParam(name = "lang", required = false, defaultValue = "") String lang,
                                                   Locale locale) {
        Locale effectiveLocale = resolveLocale(lang, locale);
        String langKey = effectiveLocale.getLanguage().startsWith("zh") ? "zh" : "en";
        List<ExchangeCapability> caps = exchangeCapabilityService.getCapabilities(langKey);
        return Map.of(
                "exchanges", caps,
                "updatedAt", java.time.LocalDateTime.now().toString()
        );
    }
}
