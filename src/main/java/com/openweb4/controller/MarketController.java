package com.openweb4.controller;

import com.openweb4.model.BtcMarketSnapshot;
import com.openweb4.service.BtcMarketService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class MarketController {

    private final BtcMarketService btcMarketService;

    public MarketController(BtcMarketService btcMarketService) {
        this.btcMarketService = btcMarketService;
    }

    @GetMapping("/api/market-forecast")
    @ResponseBody
    public Map<String, Object> marketForecastApi(@RequestParam(name = "refresh", required = false, defaultValue = "0") int refresh) {
        BtcMarketSnapshot snapshot = btcMarketService.getSnapshot(refresh == 1);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("currentPrice", snapshot.getCurrentPrice());
        payload.put("updatedAt", snapshot.getUpdatedAt());
        payload.put("forecastMethod", snapshot.getForecastMethod());
        payload.put("powerLawFairValue", snapshot.getPowerLawFairValue());
        payload.put("currentVsPowerLaw", snapshot.getCurrentVsPowerLaw());
        payload.put("drawdownFromAth", snapshot.getDrawdownFromAth());
        payload.put("dataSource", snapshot.getDataSource());
        payload.put("forecasts", snapshot.getForecasts());
        payload.put("yearlyHistory", snapshot.getYearlyHistory());
        payload.put("forecastNotes", snapshot.getForecastNotes());
        return payload;
    }

    @GetMapping("/api/market-indices")
    @ResponseBody
    public Map<String, Object> marketIndicesApi(@RequestParam(name = "refresh", required = false, defaultValue = "0") int refresh) {
        BtcMarketSnapshot snapshot = btcMarketService.getSnapshot(refresh == 1);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("currentPrice", snapshot.getCurrentPrice());
        payload.put("updatedAt", snapshot.getUpdatedAt());
        payload.put("fearGreedValue", snapshot.getFearGreedValue());
        payload.put("fearGreedLabel", snapshot.getFearGreedLabel());
        payload.put("dataSource", snapshot.getDataSource());
        payload.put("indices", snapshot.getIndices());
        return payload;
    }
}
