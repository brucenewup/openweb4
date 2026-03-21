package com.openweb4.controller;

import com.openweb4.service.AiChatService;
import com.openweb4.service.InputSanitizer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class ChatController {

    private final AiChatService aiChatService;
    private final InputSanitizer inputSanitizer;
    private final ExecutorService executor = Executors.newFixedThreadPool(10, new ThreadFactory() {
        private final AtomicInteger n = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "chat-sse-" + n.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    public ChatController(AiChatService aiChatService, InputSanitizer inputSanitizer) {
        this.aiChatService = aiChatService;
        this.inputSanitizer = inputSanitizer;
    }

    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("message") String message) {
        SseEmitter emitter = new SseEmitter(35_000L);

        String sanitized = inputSanitizer.sanitizeChat(message);
        if (sanitized.isBlank()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("[错误] 消息内容为空"));
                emitter.complete();
            } catch (IOException ignored) {}
            return emitter;
        }

        executor.submit(() -> {
            try {
                aiChatService.streamReply(sanitized, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("[错误] " + e.getMessage()));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
