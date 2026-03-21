package com.openweb4.model;

import java.time.LocalDateTime;

public class TweetItem {
    private String id;
    private String text;
    private String summary;
    private String url;
    private LocalDateTime publishedAt;
    private String source;
    private LocalDateTime fetchedAt;
    private TweetAuthor author;

    public TweetItem() {
    }

    public TweetItem(String id, String text, String summary, String url, LocalDateTime publishedAt, String source, LocalDateTime fetchedAt, TweetAuthor author) {
        this.id = id;
        this.text = text;
        this.summary = summary;
        this.url = url;
        this.publishedAt = publishedAt;
        this.source = source;
        this.fetchedAt = fetchedAt;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public TweetAuthor getAuthor() {
        return author;
    }
}
