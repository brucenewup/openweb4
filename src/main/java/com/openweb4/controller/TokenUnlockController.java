package com.openweb4.controller;

import com.openweb4.service.TokenUnlockService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API for Token Unlock Calendar.
 * Endpoint: GET /api/token-unlocks?lang=zh|en
 */
@Controller
public class TokenUnlockController {

    private final TokenUnlockService tokenUnlockService;

    public TokenUnlockController(TokenUnlockService tokenUnlockService) {
        this.tokenUnlockService = tokenUnlockService;
    }

    @GetMapping("/api/token-unlocks")
    @ResponseBody
    public Map<String, Object> tokenUnlocks(
            @RequestParam(name = "lang", defaultValue = "zh") String lang) {
        List<Map<String, Object>> unlocks = tokenUnlockService.getUpcomingUnlocks(lang);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("lang", lang);
        resp.put("updatedAt", java.time.LocalDateTime.now().toString());
        resp.put("total", unlocks.size());
        resp.put("unlocks", unlocks);
        return resp;
    }
}
