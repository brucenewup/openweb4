package com.openweb4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // Match SPA routes but NOT static assets (which contain dots, e.g. .js/.css/.svg/.html)
    @GetMapping({"/app", "/app/{path:[^\\.]*}", "/app/**/{path:[^\\.]*}"})
    public String app() {
        return "forward:/app/index.html";
    }
}

