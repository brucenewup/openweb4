package com.openweb4.model;

/**
 * 加密市值热力图数据模型
 */
public class MarketCapItem {
    private String id;
    private String symbol;
    private String name;
    private double marketCap;
    private double marketCapChange24h;
    private double marketCapPercent; // 占总市值百分比
    private String color; // 热力图颜色

    public MarketCapItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }
    public double getMarketCapChange24h() { return marketCapChange24h; }
    public void setMarketCapChange24h(double marketCapChange24h) { this.marketCapChange24h = marketCapChange24h; }
    public double getMarketCapPercent() { return marketCapPercent; }
    public void setMarketCapPercent(double marketCapPercent) { this.marketCapPercent = marketCapPercent; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
