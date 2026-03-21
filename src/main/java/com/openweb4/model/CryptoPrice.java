package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CryptoPrice {
    private String symbol;
    private BigDecimal price;
    private BigDecimal change24h;
    private BigDecimal marketCap;
    private LocalDateTime lastUpdated;

    public CryptoPrice() {}

    public CryptoPrice(String symbol, BigDecimal price, BigDecimal change24h, BigDecimal marketCap, LocalDateTime lastUpdated) {
        this.symbol = symbol;
        this.price = price;
        this.change24h = change24h;
        this.marketCap = marketCap;
        this.lastUpdated = lastUpdated;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getChange24h() { return change24h; }
    public void setChange24h(BigDecimal change24h) { this.change24h = change24h; }
    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
