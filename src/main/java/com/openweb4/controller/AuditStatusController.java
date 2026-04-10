package com.openweb4.controller;

import com.openweb4.service.AuditStatusService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API for Smart Contract Audit Status.
 * Endpoint: GET /api/audit-status?lang=zh|en
 */
@Controller
public class AuditStatusController {

    private final AuditStatusService auditStatusService;

    public AuditStatusController(AuditStatusService auditStatusService) {
        this.auditStatusService = auditStatusService;
    }

    @GetMapping("/api/audit-status")
    @ResponseBody
    public Map<String, Object> auditStatus(
            @RequestParam(name = "lang", defaultValue = "zh") String lang) {
        List<Map<String, Object>> audits = auditStatusService.getAllAudits(lang);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("lang", lang);
        resp.put("updatedAt", java.time.LocalDateTime.now().toString());
        resp.put("total", audits.size());
        resp.put("audits", audits);
        return resp;
    }
}
