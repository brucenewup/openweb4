package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.HotCoin;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class HotCoinService {

    private static final Logger log = LoggerFactory.getLogger(HotCoinService.class);
    private static final String COINGECKO_TRENDING_URL = "https://api.coingecko.com/api/v3/search/trending";
    private static final int CACHE_MINUTES = 5;

    private final OkHttpClient httpClient;
    private final Cache<String, List<HotCoin>> cache;

    public HotCoinService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }

    public List<HotCoin> getTrendingCoins() {
        String cacheKey = "trending_coins";
        List<HotCoin> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            List<HotCoin> coins = fetchFromApi();
            if (!coins.isEmpty()) {
                cache.put(cacheKey, coins);
            }
            return coins;
        } catch (Exception e) {
            log.warn("Failed to fetch trending coins: {}", e.getMessage());
            return getFallbackCoins();
        }
    }

    private List<HotCoin> fetchFromApi() {
        List<HotCoin> coins = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url(COINGECKO_TRENDING_URL)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OpenWeb4Bot/1.0)")
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("CoinGecko trending API error: HTTP {}", response.code());
                    return getFallbackCoins();
                }

                String body = response.body().string();
                JsonObject root = JsonParser.parseString(body).getAsJsonObject();

                if (!root.has("coins")) {
                    return getFallbackCoins();
                }

                JsonArray coinsArray = root.getAsJsonArray("coins");
                int rank = 1;
                for (JsonElement element : coinsArray) {
                    try {
                        JsonObject coinObj = element.getAsJsonObject();
                        JsonObject item = coinObj.has("item") ? coinObj.getAsJsonObject("item") : coinObj;

                        String id = item.has("id") ? item.get("id").getAsString() : "";
                        String symbol = item.has("symbol") ? item.get("symbol").getAsString().toUpperCase() : "";
                        String name = item.has("name") ? item.get("name").getAsString() : "";
                        int marketCapRank = item.has("market_cap_rank") ? item.get("market_cap_rank").getAsInt() : rank;

                        // 计算热度评分：基于排名，越靠前分数越高
                        double score = Math.max(0, 100 - (rank - 1) * 3.0);

                        coins.add(new HotCoin(id, symbol, name, marketCapRank, score));
                        rank++;
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching trending coins: {}", e.getMessage());
        }
        return coins;
    }

    private List<HotCoin> getFallbackCoins() {
        List<HotCoin> fallback = new ArrayList<>();
        String[][] fallbackData = {
                {"bitcoin", "BTC", "Bitcoin", "1"},
                {"ethereum", "ETH", "Ethereum", "2"},
                {"tether", "USDT", "Tether", "3"},
                {"binancecoin", "BNB", "BNB", "4"},
                {"solana", "SOL", "Solana", "5"},
                {"ripple", "XRP", "XRP", "6"},
                {"usd-coin", "USDC", "USD Coin", "7"},
                {"cardano", "ADA", "Cardano", "8"},
                {"avalanche-2", "AVAX", "Avalanche", "9"},
                {"dogecoin", "DOGE", "Dogecoin", "10"}
        };

        for (int i = 0; i < fallbackData.length; i++) {
            String[] d = fallbackData[i];
            fallback.add(new HotCoin(d[0], d[1], d[2], Integer.parseInt(d[3]), 100 - i * 3.0));
        }
        return fallback;
    }
}
