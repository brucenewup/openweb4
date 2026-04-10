package com.openweb4.model;

/**
 * KOL 情绪分析结果模型
 */
public class SentimentResult {
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private double positiveRatio; // 0-100 百分比
    private String overallTrend; // "bullish", "bearish", "neutral"
    private java.util.List<SentimentTweet> positiveTweets;
    private java.util.List<SentimentTweet> negativeTweets;

    public SentimentResult() {
        this.positiveTweets = new java.util.ArrayList<>();
        this.negativeTweets = new java.util.ArrayList<>();
    }

    public int getPositiveCount() { return positiveCount; }
    public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }
    public int getNegativeCount() { return negativeCount; }
    public void setNegativeCount(int negativeCount) { this.negativeCount = negativeCount; }
    public int getNeutralCount() { return neutralCount; }
    public void setNeutralCount(int neutralCount) { this.neutralCount = neutralCount; }
    public double getPositiveRatio() { return positiveRatio; }
    public void setPositiveRatio(double positiveRatio) { this.positiveRatio = positiveRatio; }
    public String getOverallTrend() { return overallTrend; }
    public void setOverallTrend(String overallTrend) { this.overallTrend = overallTrend; }
    public java.util.List<SentimentTweet> getPositiveTweets() { return positiveTweets; }
    public void setPositiveTweets(java.util.List<SentimentTweet> positiveTweets) { this.positiveTweets = positiveTweets; }
    public java.util.List<SentimentTweet> getNegativeTweets() { return negativeTweets; }
    public void setNegativeTweets(java.util.List<SentimentTweet> negativeTweets) { this.negativeTweets = negativeTweets; }

    public static class SentimentTweet {
        private String author;
        private String handle;
        private String text;
        private double score; // -100 到 100
        private String publishedAt;

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getHandle() { return handle; }
        public void setHandle(String handle) { this.handle = handle; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    }
}
