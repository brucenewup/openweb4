package com.openweb4.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 配置校验器：在应用启动后检查必需的配置项
 */
@Component
public class ConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    private final AppProperties appProperties;

    public ConfigValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        validateAiConfig();
        validateWhaleAlertConfig();
    }

    private void validateAiConfig() {
        AppProperties.Ai ai = appProperties.getAi();
        if (ai.getApiKey() == null || ai.getApiKey().trim().isEmpty()) {
            log.error("⚠️  AI_API_KEY is not configured! AI chat feature will not work properly.");
            log.error("    Please set environment variable: export AI_API_KEY=your_api_key");
        } else {
            log.info("✅ AI configuration validated: model={}, baseUrl={}", ai.getModel(), ai.getBaseUrl());
        }
    }

    private void validateWhaleAlertConfig() {
        AppProperties.WhaleAlert whaleAlert = appProperties.getWhaleAlert();
        if (whaleAlert.getApiKey() == null || whaleAlert.getApiKey().trim().isEmpty()) {
            log.warn("⚠️  WHALE_ALERT_API_KEY is not configured! Whale tracking will use cached data only.");
            log.warn("    To enable real-time whale tracking, set: export WHALE_ALERT_API_KEY=your_api_key");
        } else {
            log.info("✅ Whale Alert configuration validated: minValue=${}, limit={}", 
                    whaleAlert.getMinValue(), whaleAlert.getLimit());
        }
    }
}
