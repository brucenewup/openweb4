package com.openweb4.controller;

import com.openweb4.service.PerpetualFundingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * API for Binance perpetual futures long/short ratio.
 * Endpoint: GET /api/perpetual-funding?symbol=BTCUSDT
 */
@Controller
public class PerpetualController {

    private final PerpetualFundingService perpetualFundingService;

    public PerpetualController(PerpetualFundingService perpetualFundingService) {
        this.perpetualFundingService = perpetualFundingService;
    }

    @GetMapping("/api/perpetual-funding")
    @ResponseBody
    public Map<String, Object> perpetualFunding(
            @RequestParam(name = "symbol", defaultValue = "BTCUSDT") String symbol) {
        return perpetualFundingService.getLongShortRatio(symbol);
    }
}
