package com.openweb4.config;

import com.openweb4.service.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AppProperties appProperties;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           AppProperties appProperties) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.appProperties = appProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // /ws/chat — new standalone endpoint, no OpenClaw dependency
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins(appProperties.getSecurity().getAllowedOrigins());
    }
}
