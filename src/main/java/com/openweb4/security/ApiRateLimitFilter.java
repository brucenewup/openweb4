package com.openweb4.security;

import com.openweb4.config.AppProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 防刷限流：对带 refresh=1 的 GET 请求与 AI 流式对话 POST 按客户端 IP 做固定窗口计数。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final FixedWindowLimiter refreshLimiter = new FixedWindowLimiter();
    private final FixedWindowLimiter chatLimiter = new FixedWindowLimiter();

    public ApiRateLimitFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        AppProperties.RateLimit cfg = appProperties.getRateLimit();
        if (!cfg.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String ip = clientIp(request, cfg.isTrustXForwardedFor());

        if ("POST".equalsIgnoreCase(method) && "/api/chat/stream".equals(path)) {
            long windowMs = Math.max(1, cfg.getChatWindowSeconds()) * 1000L;
            int max = Math.max(1, cfg.getChatMaxPerWindow());
            if (!chatLimiter.tryAcquire("c:" + ip, windowMs, max)) {
                deny429(response, cfg.getChatWindowSeconds(), "请求过于频繁，请稍后再试（AI 对话限流）。");
                return;
            }
        } else if ("GET".equalsIgnoreCase(method)) {
            String refresh = request.getParameter("refresh");
            if ("1".equals(refresh)) {
                long windowMs = Math.max(1, cfg.getRefreshWindowSeconds()) * 1000L;
                int max = Math.max(1, cfg.getRefreshMaxPerWindow());
                if (!refreshLimiter.tryAcquire("r:" + ip, windowMs, max)) {
                    deny429(response, cfg.getRefreshWindowSeconds(), "请求过于频繁，请稍后再试（刷新限流）。");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request, boolean trustXForwardedFor) {
        if (trustXForwardedFor) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private void deny429(HttpServletResponse response, int retryAfterSeconds, String message) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(Math.max(1, retryAfterSeconds)));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = "{\"error\":\"rate_limit\",\"message\":\"" + escapeJson(message) + "\"}";
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 固定时间窗口计数（线程安全实现）。
     * 使用 ConcurrentHashMap<key, Slot> + AtomicInteger 替代原来的 mutable Slot.count + boolean[] hack。
     */
    static final class FixedWindowLimiter {
        private final ConcurrentHashMap<String, Slot> map = new ConcurrentHashMap<>();

        boolean tryAcquire(String key, long windowMs, int max) {
            long wid = System.currentTimeMillis() / windowMs;
            // 获取或创建当前窗口的 Slot
            Slot slot = map.compute(key, (k, v) -> {
                if (v == null || v.windowId != wid) {
                    return new Slot(wid);
                }
                return v;
            });
            // 原子递增并检查是否超限
            int current = slot.count.incrementAndGet();
            return current <= max;
        }

        static final class Slot {
            final long windowId;
            final AtomicInteger count;

            Slot(long windowId) {
                this.windowId = windowId;
                this.count = new AtomicInteger(0);
            }
        }
    }
}
