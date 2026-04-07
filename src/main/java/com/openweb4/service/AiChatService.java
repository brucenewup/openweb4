package com.openweb4.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.config.AppProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final MediaType JSON = MediaType.get("application/json");

    private final AppProperties appProperties;
    private final OkHttpClient httpClient;

    public AiChatService(AppProperties appProperties, @Qualifier("aiHttpClient") OkHttpClient httpClient) {
        this.appProperties = appProperties;
        this.httpClient = httpClient;
    }

    public void streamReply(String userMessage, Consumer<String> onChunk) {
        AppProperties.Ai cfg = appProperties.getAi();
        if (cfg.getApiKey() == null || cfg.getApiKey().trim().isEmpty()) {
            onChunk.accept("[错误] AI_API_KEY 未配置，请先在环境变量中设置 AI_API_KEY。");
            return;
        }

        String bodyStr = buildRequestBody(cfg, userMessage);
        String endpoint = cfg.getBaseUrl() + "/chat/completions";

        log.info("AI request: POST {} model={}", endpoint, cfg.getModel());
        Request request = new Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer " + cfg.getApiKey())
                .header("Content-Type", "application/json")
                .header("User-Agent", "python-requests/2.31.0")
                .header("Accept", "*/*")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .post(RequestBody.create(bodyStr, JSON))
                .build();



        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                log.warn("AI API error: {} | endpoint={} | errBody={}", response.code(), endpoint, errBody);
                onChunk.accept("[错误] AI 服务返回错误（" + response.code() + "），请稍后再试。");
                return;
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                onChunk.accept("[错误] AI 服务返回空响应。");
                return;
            }

            // 非流式：一次性读取完整响应，再按句子分块发送（模拟流式体验）
            String respStr = responseBody.string();
            boolean gotContent = false;
            try {
                JsonObject obj = JsonParser.parseString(respStr).getAsJsonObject();
                JsonArray choices = obj.getAsJsonArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice != null && choice.has("message")) {
                        JsonObject message = choice.getAsJsonObject("message");
                        if (message != null && message.has("content") && !message.get("content").isJsonNull()) {
                            String fullContent = message.get("content").getAsString();
                        // 按标点分块，模拟流式输出
                        String[] sentences = fullContent.split("(?<=[。！？.!?\n])");
                        for (String sentence : sentences) {
                            if (!sentence.isEmpty()) {
                                onChunk.accept(sentence);
                                gotContent = true;
                            }
                        }
                        // 若无标点分割，直接整段发送
                            if (!gotContent && !fullContent.isEmpty()) {
                                onChunk.accept(fullContent);
                                gotContent = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse AI response: {}", e.getMessage());
            }

            if (!gotContent) {
                onChunk.accept("[错误] AI 服务未返回内容，请稍后再试。");
            }

        } catch (IOException e) {
            log.warn("AI request failed: {}", e.getMessage());
            String reason = e.getMessage() != null && e.getMessage().contains("timeout")
                    ? "连接超时（30s），AI 服务当前不可用，请稍后再试。"
                    : "请求失败（" + e.getMessage() + "），请稍后再试。";
            onChunk.accept("[错误] " + reason);
        }
    }

    private String buildRequestBody(AppProperties.Ai cfg, String userMessage) {
        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.getModel());
        body.addProperty("stream", false);
        body.addProperty("max_tokens", cfg.getMaxTokens());

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content",
                "You are a professional crypto and Web3 AI assistant. Answer questions about blockchain, DeFi, NFT, digital assets and market trends. Always reply in the same language as the user's message.");
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        body.add("messages", messages);
        return body.toString();
    }
}
