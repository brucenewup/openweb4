package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.config.AppProperties;
import com.openweb4.model.WhaleTransaction;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WhaleTrackingService {

    private static final Logger log = LoggerFactory.getLogger(WhaleTrackingService.class);
    private static final String WHALE_ALERT_API_URL = "https://api.whale-alert.io/v1/transactions";

    private final AppProperties appProperties;
    private final OkHttpClient httpClient;
    private final Cache<String, List<WhaleTransaction>> cache;

    public WhaleTrackingService(AppProperties appProperties, OkHttpClient httpClient) {
        this.appProperties = appProperties;
        this.httpClient = httpClient;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(10)
                .build();
    }

    public List<WhaleTransaction> getRecentWhaleTransactions() {
        String cacheKey = "recent_whale_transactions";
        List<WhaleTransaction> cached = cache.getIfPresent(cacheKey);
        
        AppProperties.WhaleAlert cfg = appProperties.getWhaleAlert();
        if (cfg.getApiKey() == null || cfg.getApiKey().trim().isEmpty()) {
            log.warn("Whale Alert API key not configured, returning cached data or empty list");
            return cached != null ? cached : new ArrayList<>();
        }

        try {
            long currentTime = System.currentTimeMillis() / 1000;
            long startTime = currentTime - 3600; // Last 1 hour

            String url = String.format("%s?api_key=%s&start=%d&min_value=%d&limit=%d",
                    WHALE_ALERT_API_URL,
                    cfg.getApiKey(),
                    startTime,
                    cfg.getMinValue(),
                    cfg.getLimit());

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "OpenWeb4/1.0")
                    .build();

            log.debug("Fetching whale transactions from Whale Alert API");
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    log.warn("Whale Alert API error: {} | errBody={}", response.code(), errBody);
                    return cached != null ? cached : new ArrayList<>();
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                List<WhaleTransaction> transactions = parseWhaleAlertResponse(responseBody);
                
                if (!transactions.isEmpty()) {
                    cache.put(cacheKey, transactions);
                    log.info("Fetched {} whale transactions from API", transactions.size());
                }
                
                return transactions;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch whale transactions: {}", e.getMessage());
            return cached != null ? cached : new ArrayList<>();
        }
    }

    private List<WhaleTransaction> parseWhaleAlertResponse(String responseBody) {
        List<WhaleTransaction> transactions = new ArrayList<>();
        
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (!root.has("transactions")) {
                return transactions;
            }

            JsonArray txArray = root.getAsJsonArray("transactions");
            
            for (JsonElement element : txArray) {
                JsonObject tx = element.getAsJsonObject();
                
                String hash = tx.has("hash") ? tx.get("hash").getAsString() : "";
                String symbol = tx.has("symbol") ? tx.get("symbol").getAsString().toUpperCase() : "UNKNOWN";
                
                BigDecimal amount = tx.has("amount") ? 
                        new BigDecimal(tx.get("amount").getAsString()) : BigDecimal.ZERO;
                
                BigDecimal usdValue = tx.has("amount_usd") ? 
                        new BigDecimal(tx.get("amount_usd").getAsString()) : BigDecimal.ZERO;
                
                JsonObject from = tx.has("from") ? tx.getAsJsonObject("from") : new JsonObject();
                String fromAddress = from.has("address") ? from.get("address").getAsString() : "unknown";
                
                JsonObject to = tx.has("to") ? tx.getAsJsonObject("to") : new JsonObject();
                String toAddress = to.has("address") ? to.get("address").getAsString() : "unknown";
                
                long timestamp = tx.has("timestamp") ? tx.get("timestamp").getAsLong() : System.currentTimeMillis() / 1000;
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(timestamp), 
                        ZoneId.systemDefault());
                
                transactions.add(new WhaleTransaction(
                        hash, symbol, amount, usdValue, fromAddress, toAddress, dateTime));
            }
            
            transactions.sort(Comparator.comparing(WhaleTransaction::getTimestamp).reversed());
            
        } catch (Exception e) {
            log.warn("Failed to parse Whale Alert response: {}", e.getMessage());
        }
        
        return transactions;
    }

    public List<WhaleTransaction> getHistoricalFlowData() {
        // Keep mock data for historical flow visualization
        List<WhaleTransaction> flows = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            flows.add(new WhaleTransaction(
                    "flow-in-" + i, "BTC",
                    new BigDecimal(String.valueOf(50 + i * 10L)),
                    new BigDecimal(String.valueOf(3500000 + i * 500000L)),
                    "inflow", "exchange",
                    LocalDateTime.now().minusHours(i)));
            flows.add(new WhaleTransaction(
                    "flow-out-" + i, "BTC",
                    new BigDecimal(String.valueOf(45 + i * 8L)),
                    new BigDecimal(String.valueOf(2800000 + i * 420000L)),
                    "outflow", "wallet",
                    LocalDateTime.now().minusHours(i)));
        }
        for (int i = 23; i >= 0; i--) {
            flows.add(new WhaleTransaction(
                    "eth-flow-in-" + i, "ETH",
                    new BigDecimal(String.valueOf(800 + i * 120L)),
                    new BigDecimal(String.valueOf(2800000 + i * 420000L)),
                    "inflow", "exchange",
                    LocalDateTime.now().minusHours(i)));
            flows.add(new WhaleTransaction(
                    "eth-flow-out-" + i, "ETH",
                    new BigDecimal(String.valueOf(650 + i * 95L)),
                    new BigDecimal(String.valueOf(2300000 + i * 330000L)),
                    "outflow", "wallet",
                    LocalDateTime.now().minusHours(i)));
        }
        flows.sort(Comparator.comparing(WhaleTransaction::getTimestamp));
        return flows;
    }
}
