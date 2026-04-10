package com.openweb4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.PerpetualFundingSnapshot;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Fetches Binance perpetual futures long/short account ratio.
 * Uses Caffeine cache with graceful degradation on API errors.
 */
@Service
public class PerpetualFundingService {

    private static final Logger log = LoggerFactory.getLogger(PerpetualFundingService.class);
    private static final String BINANCE_API = "https://fapi.binance.com/futures/data/globalLongShortAccountRatio";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Cache<String, Object> cache;

    public PerpetualFundingService(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(20)
                .build();
    }

    /**
     * Returns the latest long/short ratio for the given symbol.
     */
    public Map<String, Object> getLongShortRatio(String symbol) {
        String cacheKey = "ls_ratio_" + symbol;
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            String url = BINANCE_API + "?symbol=" + symbol.toUpperCase() + "&periodType=5m&limit=10";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            try (Response resp = httpClient.newCall(request).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    log.warn("Binance perpetual API error: HTTP {}", resp.code());
                    return buildFallback(symbol, "API error: HTTP " + resp.code());
                }

                String body = resp.body().string();
                return parseResponse(symbol, body, cacheKey);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch perpetual ratio for {}: {}", symbol, e.getMessage());
            return buildFallback(symbol, e.getMessage());
        }
    }

    private Map<String, Object> parseResponse(String symbol, String body, String cacheKey) {
        try {
            JsonArray arr = JsonParser.parseString(body).getAsJsonArray();
            if (arr == null || arr.isEmpty()) {
                return buildFallback(symbol, "Empty response");
            }

            // Use the most recent entry
            JsonObject latest = arr.get(arr.size() - 1).getAsJsonObject();

            String sym = symbol.toUpperCase();
            BigDecimal longShortRatio = new BigDecimal(latest.get("longShortRatio").getAsString());
            BigDecimal longAccount = new BigDecimal(latest.get("longAccount").getAsString());
            BigDecimal shortAccount = new BigDecimal(latest.get("shortAccount").getAsString());
            long ts = latest.has("updateTime") ? latest.get("updateTime").getAsLong() : System.currentTimeMillis();

            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
            String timeStr = dt.format(ISO_FMT);

            // Build history for chart
            List<Map<String, Object>> history = new ArrayList<>();
            for (int i = arr.size() - 1; i >= 0; i--) {
                JsonObject o = arr.get(i).getAsJsonObject();
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("timestamp", o.get("updateTime").getAsLong());
                point.put("time", LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(o.get("updateTime").getAsLong()),
                        ZoneId.systemDefault()).format(ISO_FMT));
                point.put("longShortRatio", new BigDecimal(o.get("longShortRatio").getAsString()));
                point.put("longAccount", new BigDecimal(o.get("longAccount").getAsString()));
                point.put("shortAccount", new BigDecimal(o.get("shortAccount").getAsString()));
                history.add(point);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("symbol", sym);
            result.put("longShortRatio", longShortRatio);
            result.put("longAccount", longAccount);
            result.put("shortAccount", shortAccount);
            result.put("timestamp", ts);
            result.put("timeStr", timeStr);
            result.put("history", history);

            cache.put(cacheKey, result);
            return result;

        } catch (Exception e) {
            log.warn("Failed to parse perpetual response for {}: {}", symbol, e.getMessage());
            return buildFallback(symbol, e.getMessage());
        }
    }

    private Map<String, Object> buildFallback(String symbol, String reason) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("symbol", symbol.toUpperCase());
        fallback.put("longShortRatio", BigDecimal.ZERO);
        fallback.put("longAccount", BigDecimal.ZERO);
        fallback.put("shortAccount", BigDecimal.ZERO);
        fallback.put("timestamp", System.currentTimeMillis());
        fallback.put("timeStr", LocalDateTime.now().format(ISO_FMT));
        fallback.put("history", List.of());
        fallback.put("fallback", true);
        fallback.put("fallbackReason", reason);
        return fallback;
    }
}
