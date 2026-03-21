package com.openweb4.config;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 全局基础设施配置：共享 OkHttpClient Bean，避免各 Service 各自创建实例。
 */
@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    @Primary
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(java.util.Arrays.asList(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1));

        // Java 不自动读取 HTTPS_PROXY/HTTP_PROXY 环境变量，需手动设置
        String proxyUrl = System.getenv("HTTPS_PROXY");
        if (proxyUrl == null || proxyUrl.isEmpty()) proxyUrl = System.getenv("https_proxy");
        if (proxyUrl == null || proxyUrl.isEmpty()) proxyUrl = System.getenv("HTTP_PROXY");
        if (proxyUrl == null || proxyUrl.isEmpty()) proxyUrl = System.getenv("http_proxy");

        String noProxyEnv = System.getenv("NO_PROXY");
        if (noProxyEnv == null || noProxyEnv.isEmpty()) noProxyEnv = System.getenv("no_proxy");
        final List<String> noProxyHosts = (noProxyEnv != null && !noProxyEnv.isEmpty())
                ? Arrays.asList(noProxyEnv.split(","))
                : Collections.emptyList();

        if (proxyUrl != null && !proxyUrl.isEmpty()) {
            try {
                URI proxyUri = new URI(proxyUrl);
                final Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
                log.info("OkHttpClient using proxy: {}:{}, noProxyHosts={}",
                        proxyUri.getHost(), proxyUri.getPort(), noProxyHosts);

                builder.proxySelector(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        String host = uri.getHost();
                        for (String noProxy : noProxyHosts) {
                            String trim = noProxy.trim();
                            if (!trim.isEmpty() && host != null && host.endsWith(trim)) {
                                return Collections.singletonList(Proxy.NO_PROXY);
                            }
                        }
                        return Collections.singletonList(proxy);
                    }

                    @Override
                    public void connectFailed(URI uri, SocketAddress sa, IOException e) {
                        log.warn("Proxy connect failed for {}: {}", uri, e.getMessage());
                    }
                });
            } catch (Exception e) {
                log.warn("Failed to parse proxy URL '{}': {}", proxyUrl, e.getMessage());
            }
        }

        return builder.build();
    }

    @Bean
    @Qualifier("aiHttpClient")
    public OkHttpClient aiHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1));

        // AI API 必须走代理，否则 JVM TLS 指纹被 Cloudflare Bot Management 拦截
        String httpsProxy = System.getenv("HTTPS_PROXY");
        if (httpsProxy == null || httpsProxy.isEmpty()) httpsProxy = System.getenv("https_proxy");
        if (httpsProxy == null || httpsProxy.isEmpty()) httpsProxy = System.getenv("HTTP_PROXY");
        if (httpsProxy == null || httpsProxy.isEmpty()) httpsProxy = System.getenv("http_proxy");
        if (httpsProxy != null && !httpsProxy.isEmpty()) {
            try {
                URI proxyUri = new URI(httpsProxy);
                builder.proxy(new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort())));
                log.info("aiHttpClient using HTTP proxy: {}:{}", proxyUri.getHost(), proxyUri.getPort());
            } catch (Exception e) {
                log.warn("aiHttpClient: proxy parse failed: {}", e.getMessage());
                builder.proxy(Proxy.NO_PROXY);
            }
        } else {
            builder.proxy(Proxy.NO_PROXY);
            log.warn("aiHttpClient: no proxy configured — Cloudflare may block JVM TLS fingerprint");
        }

        return builder.build();
    }
}
