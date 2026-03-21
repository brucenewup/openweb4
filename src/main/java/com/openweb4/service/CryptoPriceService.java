package com.openweb4.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.CryptoPrice;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CryptoPriceService {

    private static final Logger log = LoggerFactory.getLogger(CryptoPriceService.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final OkHttpClient client;
    private final Map<String, CryptoPrice> cache = new ConcurrentHashMap<>();

    public CryptoPriceService(OkHttpClient client) {
        this.client = client;
    }

    public CryptoPrice getBitcoinPrice() {
        return getPrice("bitcoin", "BTC");
    }

    public CryptoPrice getEthereumPrice() {
        return getPrice("ethereum", "ETH");
    }

    public CryptoPrice getTetherPrice() {
        return getPrice("tether", "USDT");
    }

    private CryptoPrice getPrice(String coinId, String symbol) {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinId
                + "&vs_currencies=usd&include_24hr_change=true&include_market_cap=true";

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Accept", "application/json")
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP " + response.code());
                }
                if (response.body() == null) {
                    throw new IOException("Empty response body");
                }

                JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();
                if (!root.has(coinId) || root.get(coinId).isJsonNull()) {
                    throw new IOException("Missing price payload for " + coinId);
                }

                JsonObject coin = root.getAsJsonObject(coinId);
                CryptoPrice cryptoPrice = new CryptoPrice(
                        symbol,
                        coin.has("usd") && !coin.get("usd").isJsonNull() ? coin.get("usd").getAsBigDecimal() : ZERO,
                        coin.has("usd_24h_change") && !coin.get("usd_24h_change").isJsonNull()
                                ? coin.get("usd_24h_change").getAsBigDecimal() : ZERO,
                        coin.has("usd_market_cap") && !coin.get("usd_market_cap").isJsonNull()
                                ? coin.get("usd_market_cap").getAsBigDecimal() : ZERO,
                        LocalDateTime.now()
                );
                cache.put(symbol, cryptoPrice);
                return cryptoPrice;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch {} price from CoinGecko: {}", symbol, e.getMessage());
        }

        CryptoPrice cached = cache.get(symbol);
        if (cached != null) {
            return cached;
        }

        return new CryptoPrice(symbol, ZERO, ZERO, ZERO, LocalDateTime.now());
    }
}
