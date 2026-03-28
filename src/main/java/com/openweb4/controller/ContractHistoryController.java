package com.openweb4.controller;

import com.openweb4.service.ContractHistoryService;
import com.openweb4.service.ContractHistoryService.Snapshot;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contract")
public class ContractHistoryController {

    private final ContractHistoryService contractHistoryService;

    public ContractHistoryController(ContractHistoryService contractHistoryService) {
        this.contractHistoryService = contractHistoryService;
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getHistory(
            @RequestParam(value = "symbol", defaultValue = "BTC") String symbol) {
        List<Snapshot> snapshots = contractHistoryService.getHistory(symbol);
        return snapshots.stream().map(s -> Map.<String, Object>of(
            "timestamp", s.timestamp,
            "longShortRatio", s.longShortRatio,
            "longAccount", s.longAccount,
            "shortAccount", s.shortAccount
        )).collect(Collectors.toList());
    }
}
