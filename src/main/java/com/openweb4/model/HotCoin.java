package com.openweb4.model;

/**
 * 山寨币热度数据模型
 */
public class HotCoin {
    private String id;
    private String symbol;
    private String name;
    private int marketCapRank;
    private double score; // 热度评分 0-100

    public HotCoin() {}

    public HotCoin(String id, String symbol, String name, int marketCapRank, double score) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.marketCapRank = marketCapRank;
        this.score = score;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMarketCapRank() { return marketCapRank; }
    public void setMarketCapRank(int marketCapRank) { this.marketCapRank = marketCapRank; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
