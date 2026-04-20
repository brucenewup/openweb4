package com.openweb4.controller;

import com.openweb4.model.RwaProject;
import com.openweb4.service.RwaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API for Real-World Asset (RWA) tokenization projects.
 * Endpoint: GET /api/rwa-projects?lang=zh|en
 */
@Controller
public class RwaController {

    private final RwaService rwaService;

    public RwaController(RwaService rwaService) {
        this.rwaService = rwaService;
    }

    @GetMapping("/api/rwa-projects")
    @ResponseBody
    public Map<String, Object> getRwaProjects(
            @RequestParam(name = "lang", defaultValue = "zh") String lang) {
        List<RwaProject> projects = rwaService.getRwaProjects();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("lang", lang);
        resp.put("updatedAt", java.time.LocalDateTime.now().toString());
        resp.put("total", projects.size());
        resp.put("projects", projects);
        
        // Calculate total TVL
        java.math.BigDecimal totalTvl = projects.stream()
                .map(RwaProject::getTvl)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        resp.put("totalTvl", totalTvl);

        return resp;
    }
}
