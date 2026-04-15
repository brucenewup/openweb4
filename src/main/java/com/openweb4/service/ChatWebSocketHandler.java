package com.openweb4.service;

import com.google.gson.Gson;
import com.openweb4.config.AppProperties;
import com.openweb4.util.RateLimitSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standalone WebSocket handler for the AI Chat page.
 * Decoupled from OpenClaw – processes messages via the injected AiChatService.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** WS 每 IP 每分钟最大消息数 */
    private static final int WS_MAX_PER_WINDOW = 10;
    private static final long WS_WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    /** per-IP 限流计数：key=ip, value=RateLimitSlot */
    private final ConcurrentHashMap<String, RateLimitSlot> wsRateLimiter = new ConcurrentHashMap<>();
    private final AppProperties appProperties;
    private final AiChatService aiChatService;
    private final InputSanitizer inputSanitizer;
    private final Gson gson = new Gson();

    public ChatWebSocketHandler(AppProperties appProperties, AiChatService aiChatService, InputSanitizer inputSanitizer) {
        this.appProperties = appProperties;
        this.aiChatService = aiChatService;
        this.inputSanitizer = inputSanitizer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.put(session.getId(), session);
        log.info("Chat WS connected: {}", session.getId());
        sendJson(session, Map.of(
                "type", "connected",
                "message", "已连接，可以开始对话"
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // per-IP 限流
            String ip = extractIp(session);
            if (!wsAcquire(ip)) {
                sendJson(session, Map.of("type", "error", "message", "请求过于频繁，请稍后再试（WebSocket 限流）。"));
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = gson.fromJson(message.getPayload(), Map.class);
            String type = (String) payload.getOrDefault("type", "");
            String text = (String) payload.getOrDefault("message", "");

            if (!"chat".equals(type) || text == null || text.isBlank()) return;

            String sanitized = inputSanitizer.sanitizeChat(text);
            if (sanitized.isBlank()) {
                sendJson(session, Map.of("type", "error", "message", "消息内容为空"));
                return;
            }

            // Stream response chunks back
            aiChatService.streamReply(sanitized, chunk -> {
                try {
                    sendJson(session, Map.of("type", "chunk", "content", chunk));
                } catch (IOException e) {
                    log.warn("Failed to send chunk", e);
                }
            });

            sendJson(session, Map.of("type", "done"));

        } catch (Exception e) {
            log.error("Error handling message for session {}", session.getId(), e);
            try {
                sendJson(session, Map.of("type", "error", "message", "处理消息时出错：" + e.getMessage()));
            } catch (IOException ex) {
                log.warn("Failed to send error", ex);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("Chat WS disconnected: {} ({})", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session.getId());
        log.warn("Chat WS transport error for {}", session.getId(), exception);
    }

    private void sendJson(WebSocketSession session, Map<String, Object> data) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(gson.toJson(data)));
        }
    }

    private String extractIp(WebSocketSession session) {
        InetSocketAddress addr = session.getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    private boolean wsAcquire(String ip) {
        long wid = System.currentTimeMillis() / WS_WINDOW_MS;
        RateLimitSlot slot = wsRateLimiter.compute(ip, (k, v) -> {
            if (v == null || v.getWindowId() != wid) return new RateLimitSlot(wid);
            return v;
        });
        return slot.getCount().incrementAndGet() <= WS_MAX_PER_WINDOW;
    }
}
