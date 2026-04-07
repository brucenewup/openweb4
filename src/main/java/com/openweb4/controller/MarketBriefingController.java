package com.openweb4.controller;

import com.openweb4.service.MarketBriefingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MarketBriefingController {

    private final MarketBriefingService marketBriefingService;

    public MarketBriefingController(MarketBriefingService marketBriefingService) {
        this.marketBriefingService = marketBriefingService;
    }

    @GetMapping("/market-briefing")
    public MarketBriefingService.BriefingData getMarketBriefing() {
        return marketBriefingService.getLatestBriefing();
    }
}
