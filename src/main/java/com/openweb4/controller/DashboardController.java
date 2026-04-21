package com.openweb4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index() {
        return "redirect:/app/";
    }

    @GetMapping("/app/")
    public String app() {
        return "forward:/app/index.html";
    }

    @GetMapping({"/dashboard", "/news", "/kol-tweets", "/market-forecast", "/market-indices", "/transaction-skills", "/ai-chat"})
    public String spaFallback() {
        return "spa";
    }
}
