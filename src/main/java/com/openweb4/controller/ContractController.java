package com.openweb4.controller;

import com.openweb4.model.ContractDataDto;
import com.openweb4.service.ContractDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ContractController {

    private final ContractDataService contractDataService;

    public ContractController(ContractDataService contractDataService) {
        this.contractDataService = contractDataService;
    }

    @GetMapping("/contract")
    public ResponseEntity<ContractDataDto> getContractData(
            @RequestParam(value = "symbol", defaultValue = "BTCUSDT") String symbol) {
        ContractDataDto dto = contractDataService.getContractData(symbol);
        return ResponseEntity.ok(dto);
    }
}
