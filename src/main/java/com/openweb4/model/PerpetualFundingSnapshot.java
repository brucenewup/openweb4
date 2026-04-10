package com.openweb4.model;

import java.math.BigDecimal;

/**
 * Perpetual futures long/short ratio snapshot.
 */
public class PerpetualFundingSnapshot {

    private String symbol;
    private BigDecimal longShortRatio;
    private BigDecimal longAccount;
    private BigDecimal shortAccount;
    private Long timestamp;

    public PerpetualFundingSnapshot() {}

    public PerpetualFundingSnapshot(String symbol, BigDecimal longShortRatio,
            BigDecimal longAccount, BigDecimal shortAccount, Long timestamp) {
        this.symbol = symbol;
        this.longShortRatio = longShortRatio;
        this.longAccount = longAccount;
        this.shortAccount = shortAccount;
        this.timestamp = timestamp;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getLongShortRatio() { return longShortRatio; }
    public void setLongShortRatio(BigDecimal longShortRatio) { this.longShortRatio = longShortRatio; }
    public BigDecimal getLongAccount() { return longAccount; }
    public void setLongAccount(BigDecimal longAccount) { this.longAccount = longAccount; }
    public BigDecimal getShortAccount() { return shortAccount; }
    public void setShortAccount(BigDecimal shortAccount) { this.shortAccount = shortAccount; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
