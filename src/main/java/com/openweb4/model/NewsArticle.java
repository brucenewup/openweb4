package com.openweb4.model;

import java.time.LocalDateTime;

public class NewsArticle {
    private String title;
    private String summary;
    private String source;
    private String url;
    private LocalDateTime publishedAt;

    public NewsArticle() {}

    public NewsArticle(String title, String summary, String source, String url, LocalDateTime publishedAt) {
        this.title = title;
        this.summary = summary;
        this.source = source;
        this.url = url;
        this.publishedAt = publishedAt;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
