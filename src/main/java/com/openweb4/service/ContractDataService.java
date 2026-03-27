package com.openweb4.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.ContractDataDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ContractDataService {

    private static final Logger log = LoggerFactory.getLogger(ContractDataService.class);
    private static final String BASE_URL = "https://fapi.binance.com";
    private static final long CACHE_TTL_MS = 60_000L;

    private final OkHttpClient client;
    private final Map<String, ContractDataDto> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTime = new ConcurrentHashMap<>();

    public ContractDataService(OkHttpClient client) {
        this.client = client;
    }

    public ContractDataDto getContractData(String symbol) {
        String key = symbol.toUpperCase();
        Long lastFetch = cacheTime.get(key);
        if (lastFetch != null && System.currentTimeMillis() - lastFetch < CACHE_TTL_MS) {
            return cache.get(key);
        }
        ContractDataDto dto = fetchFromBinance(key);
        cache.put(key, dto);
        cacheTime.put(key, System.currentTimeMillis());
        return dto;
    }

    private ContractDataDto fetchFromBinance(String symbol) {
        ContractDataDto dto = new ContractDataDto();
        dto.setSymbol(symbol);
        dto.setTimestamp(System.currentTimeMillis());

        try {
            // 1. 多空账户比（大户持仓比，/fapi/v1/globalLongShortAccountRatio 在部分地区不可用）
            String lsUrl = BASE_URL + "/futures/data/topLongShortAccountRatio?symbol=" + symbol + "&period=5m&limit=1";
            String lsBody = fetch(lsUrl);
            if (lsBody != null) {
                JsonArray arr = JsonParser.parseString(lsBody).getAsJsonArray();
                if (arr.size() > 0) {
                    JsonObject obj = arr.get(0).getAsJsonObject();
                    double ratio = obj.get("longShortRatio").getAsDouble();
                    double longAcc = obj.get("longAccount").getAsDouble();
                    double shortAcc = obj.get("shortAccount").getAsDouble();
                    dto.setLongShortRatio(ratio);
                    dto.setLongAccount(longAcc * 100);
                    dto.setShortAccount(shortAcc * 100);
                }
            }

            // 2. 未平仓合约量
            String oiUrl = BASE_URL + "/fapi/v1/openInterest?symbol=" + symbol;
            String oiBody = fetch(oiUrl);
            if (oiBody != null) {
                JsonObject obj = JsonParser.parseString(oiBody).getAsJsonObject();
                double oi = obj.get("openInterest").getAsDouble();
                dto.setOpenInterest(oi);
            }

            // 3. 资金费率
            String frUrl = BASE_URL + "/fapi/v1/fundingRate?symbol=" + symbol + "&limit=1";
            String frBody = fetch(frUrl);
            if (frBody != null) {
                JsonArray arr = JsonParser.parseString(frBody).getAsJsonArray();
                if (arr.size() > 0) {
                    JsonObject obj = arr.get(0).getAsJsonObject();
                    double fr = obj.get("fundingRate").getAsDouble();
                    dto.setFundingRate(fr * 100); // 转为百分比
                }
            }

            // 4. 主动买卖量比
            String takerUrl = BASE_URL + "/futures/data/takerlongshortRatio?symbol=" + symbol + "&period=5m&limit=1";
            String takerBody = fetch(takerUrl);
            if (takerBody != null) {
                JsonArray arr = JsonParser.parseString(takerBody).getAsJsonArray();
                if (arr.size() > 0) {
                    JsonObject obj = arr.get(0).getAsJsonObject();
                    double buyRatio = obj.get("buySellRatio").getAsDouble();
                    dto.setTakerBuyRatio(buyRatio);
                    dto.setTakerSellRatio(2.0 - buyRatio); // 估算卖量比
                }
            }

        } catch (Exception e) {
            log.warn("Failed to fetch contract data for {}: {}", symbol, e.getMessage());
            dto.setError("数据获取失败: " + e.getMessage());
        }

        return dto;
    }

    private String fetch(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "OpenWeb4/1.0")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            log.warn("Binance API returned {}: {}", response.code(), url);
        } catch (IOException e) {
            log.warn("HTTP error fetching {}: {}", url, e.getMessage());
        }
        return null;
    }
}
