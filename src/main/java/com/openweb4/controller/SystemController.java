package com.openweb4.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.build-time:unknown}")
    private String buildTime;

    @GetMapping("/version")
    public Map<String, Object> getVersion() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", appVersion);
        response.put("buildTime", buildTime);
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
        return response;
    }
}
