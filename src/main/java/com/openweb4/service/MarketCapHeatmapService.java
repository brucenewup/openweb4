package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.MarketCapItem;
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
public class MarketCapHeatmapService {

    private static final Logger log = LoggerFactory.getLogger(MarketCapHeatmapService.class);
    private static final String COINGECKO_MARKETS_URL = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1&sparkline=false&price_change_percentage=24h";
    private static final int CACHE_MINUTES = 10;

    private final OkHttpClient httpClient;
    private final Cache<String, List<MarketCapItem>> cache;

    public MarketCapHeatmapService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }

    public List<MarketCapItem> getHeatmapData() {
        String cacheKey = "market_cap_heatmap";
        List<MarketCapItem> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            List<MarketCapItem> items = fetchFromApi();
            if (!items.isEmpty()) {
                cache.put(cacheKey, items);
            }
            return items;
        } catch (Exception e) {
            log.warn("Failed to fetch market cap heatmap: {}", e.getMessage());
            return getFallbackData();
        }
    }

    private List<MarketCapItem> fetchFromApi() {
        List<MarketCapItem> items = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url(COINGECKO_MARKETS_URL)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OpenWeb4Bot/1.0)")
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("CoinGecko markets API error: HTTP {}", response.code());
                    return getFallbackData();
                }

                String body = response.body().string();
                JsonArray root = JsonParser.parseString(body).getAsJsonArray();

                double totalMarketCap = 0;
                // First pass: calculate total market cap
                for (JsonElement el : root) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("market_cap")) {
                        totalMarketCap += obj.get("market_cap").getAsDouble();
                    }
                }

                // Second pass: build items
                for (JsonElement el : root) {
                    try {
                        JsonObject obj = el.getAsJsonObject();
                        MarketCapItem item = new MarketCapItem();
                        item.setId(obj.has("id") ? obj.get("id").getAsString() : "");
                        item.setSymbol(obj.has("symbol") ? obj.get("symbol").getAsString().toUpperCase() : "");
                        item.setName(obj.has("name") ? obj.get("name").getAsString() : "");

                        double marketCap = obj.has("market_cap") ? obj.get("market_cap").getAsDouble() : 0;
                        item.setMarketCap(marketCap);

                        double change24h = obj.has("price_change_percentage_24h") ? obj.get("price_change_percentage_24h").getAsDouble() : 0;
                        item.setMarketCapChange24h(change24h);

                        if (totalMarketCap > 0) {
                            item.setMarketCapPercent(marketCap / totalMarketCap * 100);
                        }

                        // Calculate color based on 24h change
                        String color = calculateColor(change24h);
                        item.setColor(color);

                        items.add(item);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching market cap heatmap: {}", e.getMessage());
        }

        items.sort(Comparator.comparingDouble(MarketCapItem::getMarketCap).reversed());
        return items;
    }

    private String calculateColor(double change24h) {
        // Green for positive, red for negative
        if (change24h >= 10) {
            return "#00ff88"; // Strong green
        } else if (change24h >= 5) {
            return "#00cc66";
        } else if (change24h >= 2) {
            return "#33cc55";
        } else if (change24h >= 0) {
            return "#66aa44";
        } else if (change24h >= -2) {
            return "#aa6644"; // Light red
        } else if (change24h >= -5) {
            return "#cc4444";
        } else if (change24h >= -10) {
            return "#ff3366";
        } else {
            return "#ff0044"; // Strong red
        }
    }

    private List<MarketCapItem> getFallbackData() {
        List<MarketCapItem> fallback = new ArrayList<>();
        String[][] fallbackData = {
                {"bitcoin", "BTC", "Bitcoin", "1200000000000", "2.5"},
                {"ethereum", "ETH", "Ethereum", "450000000000", "1.8"},
                {"tether", "USDT", "Tether", "100000000000", "0.01"},
                {"binancecoin", "BNB", "BNB", "60000000000", "3.2"},
                {"solana", "SOL", "Solana", "45000000000", "5.5"},
                {"ripple", "XRP", "XRP", "35000000000", "-1.2"},
                {"usd-coin", "USDC", "USD Coin", "32000000000", "0.02"},
                {"cardano", "ADA", "Cardano", "18000000000", "-2.8"},
                {"avalanche-2", "AVAX", "Avalanche", "15000000000", "4.1"},
                {"dogecoin", "DOGE", "Dogecoin", "12000000000", "-3.5"},
                {"polkadot", "DOT", "Polkadot", "9000000000", "1.2"},
                {"chainlink", "LINK", "Chainlink", "7000000000", "6.8"},
                {"matic-network", "MATIC", "Polygon", "6500000000", "-1.5"},
                {"shiba-inu", "SHIB", "Shiba Inu", "6000000000", "-4.2"},
                {"litecoin", "LTC", "Litecoin", "5500000000", "0.8"}
        };

        for (String[] d : fallbackData) {
            MarketCapItem item = new MarketCapItem();
            item.setId(d[0]);
            item.setSymbol(d[1]);
            item.setName(d[2]);
            item.setMarketCap(Double.parseDouble(d[3]));
            double change = Double.parseDouble(d[4]);
            item.setMarketCapChange24h(change);
            item.setColor(calculateColor(change));
            item.setMarketCapPercent(0);
            fallback.add(item);
        }
        return fallback;
    }
}
