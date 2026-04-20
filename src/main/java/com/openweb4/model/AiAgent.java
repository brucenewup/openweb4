package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Agent trading activity model
 */
public class AiAgent {
    private String id;
    private String name;
    private String protocol;
    private BigDecimal tradingVolume24h;
    private Double successRate;
    private String strategyType;
    private Boolean x402Compatible;
    private String website;
    private String dataSource;
    private LocalDateTime updatedAt;

    public AiAgent() {}

    public AiAgent(String id, String name, String protocol, BigDecimal tradingVolume24h,
                   Double successRate, String strategyType, Boolean x402Compatible,
                   String website, String dataSource, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.tradingVolume24h = tradingVolume24h;
        this.successRate = successRate;
        this.strategyType = strategyType;
        this.x402Compatible = x402Compatible;
        this.website = website;
        this.dataSource = dataSource;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public BigDecimal getTradingVolume24h() { return tradingVolume24h; }
    public void setTradingVolume24h(BigDecimal tradingVolume24h) { this.tradingVolume24h = tradingVolume24h; }
    
    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    
    public Boolean getX402Compatible() { return x402Compatible; }
    public void setX402Compatible(Boolean x402Compatible) { this.x402Compatible = x402Compatible; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
