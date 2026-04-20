package com.openweb4.controller;

import com.openweb4.model.AiAgent;
import com.openweb4.service.AiAgentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API for AI Agent trading activity monitoring.
 * Endpoint: GET /api/ai-agents?lang=zh|en
 */
@Controller
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @GetMapping("/api/ai-agents")
    @ResponseBody
    public Map<String, Object> getAiAgents(
            @RequestParam(name = "lang", defaultValue = "zh") String lang) {
        List<AiAgent> agents = aiAgentService.getAiAgents();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("lang", lang);
        resp.put("updatedAt", java.time.LocalDateTime.now().toString());
        resp.put("total", agents.size());
        resp.put("agents", agents);
        
        // Calculate total trading volume
        java.math.BigDecimal totalVolume = agents.stream()
                .map(AiAgent::getTradingVolume24h)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        resp.put("totalVolume24h", totalVolume);

        return resp;
    }
}
