package com.openweb4.service;

import com.openweb4.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * 统一输入清洗组件，供 ChatController、ChatWebSocketHandler 等共用。
 * 替代原来各处散落的 sanitize/sanitizeInput 方法。
 */
@Component
public class InputSanitizer {

    private final AppProperties appProperties;

    public InputSanitizer(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 清洗聊天输入：去除危险命令模式，截断到最大长度。
     */
    public String sanitizeChat(String input) {
        if (input == null) return "";
        String s = input
                .replaceAll("(?i)\\bcurl\\b", "")
                .replaceAll("(?i)\\bwget\\b", "")
                .replaceAll("rm\\s+-rf", "")
                .replaceAll(";\\s*\\w+", "")
                .replaceAll("`[^`]+`", "")
                .replaceAll("\\$\\([^)]+\\)", "")
                .trim();
        int max = appProperties.getSecurity().getMaxChatMessageLength();
        if (s.length() > max) s = s.substring(0, max);
        return s;
    }

    /**
     * 简单截断清洗（用于非聊天场景）。
     */
    public String sanitize(String input, int maxLen) {
        if (input == null) return "";
        String s = input.trim();
        if (s.length() > maxLen) s = s.substring(0, maxLen);
        return s;
    }
}
