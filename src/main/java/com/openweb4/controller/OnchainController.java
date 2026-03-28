package com.openweb4.controller;

import com.openweb4.model.OnchainDataDto;
import com.openweb4.service.OnchainDataService;
import org.springframework.web.bind.annotation.*;

@RestController
public class OnchainController {

    private final OnchainDataService onchainDataService;

    public OnchainController(OnchainDataService onchainDataService) {
        this.onchainDataService = onchainDataService;
    }

    /**
     * GET /api/onchain?symbol=BTC
     * 支持 BTC / ETH / SOL，默认 BTC
     */
    @GetMapping("/api/onchain")
    public OnchainDataDto onchain(
            @RequestParam(name = "symbol", required = false, defaultValue = "BTC") String symbol) {
        return onchainDataService.getData(symbol);
    }
}
