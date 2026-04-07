package com.openweb4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Ai ai = new Ai();
    private final Security security = new Security();
    private final Tweets tweets = new Tweets();
    private final RateLimit rateLimit = new RateLimit();
    private final WhaleAlert whaleAlert = new WhaleAlert();

    public Ai getAi() {
        return ai;
    }

    public Security getSecurity() {
        return security;
    }

    public Tweets getTweets() {
        return tweets;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public WhaleAlert getWhaleAlert() {
        return whaleAlert;
    }

    public static class Ai {
        private String baseUrl = "https://gmn.chuangzuoli.com/v1";
        private String apiKey = "";
        private String model = "gpt-5.2";
        private int maxTokens = 1024;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    public static class Security {
        private String[] allowedOrigins = new String[]{"http://127.0.0.1:8080", "http://localhost:8080"};
        private int maxChatMessageLength = 500;

        public String[] getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String[] allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public int getMaxChatMessageLength() { return maxChatMessageLength; }
        public void setMaxChatMessageLength(int maxChatMessageLength) { this.maxChatMessageLength = maxChatMessageLength; }
    }

    /**
     * 防刷：按客户端 IP 对「强制刷新」与 AI 对话做滑动窗口计数。
     */
    public static class RateLimit {
        /** 是否启用限流 */
        private boolean enabled = true;
        /** refresh=1 时：时间窗口长度（秒） */
        private int refreshWindowSeconds = 60;
        /** 每个 IP 在每个窗口内允许的最大 refresh=1 次数 */
        private int refreshMaxPerWindow = 20;
        /** AI 对话：时间窗口长度（秒） */
        private int chatWindowSeconds = 60;
        /** 每个 IP 在每个窗口内允许的 AI 请求次数 */
        private int chatMaxPerWindow = 30;
        /** 是否信任 X-Forwarded-For（仅在你前面有可信反向代理时开启） */
        private boolean trustXForwardedFor = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getRefreshWindowSeconds() { return refreshWindowSeconds; }
        public void setRefreshWindowSeconds(int refreshWindowSeconds) { this.refreshWindowSeconds = refreshWindowSeconds; }
        public int getRefreshMaxPerWindow() { return refreshMaxPerWindow; }
        public void setRefreshMaxPerWindow(int refreshMaxPerWindow) { this.refreshMaxPerWindow = refreshMaxPerWindow; }
        public int getChatWindowSeconds() { return chatWindowSeconds; }
        public void setChatWindowSeconds(int chatWindowSeconds) { this.chatWindowSeconds = chatWindowSeconds; }
        public int getChatMaxPerWindow() { return chatMaxPerWindow; }
        public void setChatMaxPerWindow(int chatMaxPerWindow) { this.chatMaxPerWindow = chatMaxPerWindow; }
        public boolean isTrustXForwardedFor() { return trustXForwardedFor; }
        public void setTrustXForwardedFor(boolean trustXForwardedFor) { this.trustXForwardedFor = trustXForwardedFor; }
    }

    public static class Tweets {
        // 支持的所有大V列表（完整池），默认全部启用
        private String[] handles = new String[]{
                "elonmusk",
                "cz_binance",
                "VitalikButerin",
                "CryptoHayes",
                "saylor",
                "BarrySilbert",
                "APompliano",
                "PeterSchiff",
                "TimFerriss",
                "naval",
                "TheBlock__",
                "fundstrat",
                "woonomic",
                "scottmelker",
                "MichaelvanDePoppe",
                "CryptoCobain",
                "Bitboy_Crypto",
                "CryptoRus",
                // 创作者 / AI / 增长类
                "thejustinwelsh",
                "PaddyG96",
                "rowancheung",
                "thedankoe",
                "bentossell",
                "dickiebush",
                "LinusEkenstam",
                "fortelabs",
                "steveschoger",
                "RyanHoliday"
        };

        public String[] getHandles() { return handles; }
        public void setHandles(String[] handles) { this.handles = handles; }
    }

    public static class WhaleAlert {
        private String apiKey = "";
        private int minValue = 500000;
        private int limit = 10;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public int getMinValue() { return minValue; }
        public void setMinValue(int minValue) { this.minValue = minValue; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
    }
}
