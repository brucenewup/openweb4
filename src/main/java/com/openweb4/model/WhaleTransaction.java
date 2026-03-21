package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WhaleTransaction {
    private String hash;
    private String symbol;
    private BigDecimal amount;
    private BigDecimal usdValue;
    private String fromAddress;
    private String toAddress;
    private LocalDateTime timestamp;

    public WhaleTransaction() {}

    public WhaleTransaction(String hash, String symbol, BigDecimal amount, BigDecimal usdValue,
                            String fromAddress, String toAddress, LocalDateTime timestamp) {
        this.hash = hash;
        this.symbol = symbol;
        this.amount = amount;
        this.usdValue = usdValue;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.timestamp = timestamp;
    }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getUsdValue() { return usdValue; }
    public void setUsdValue(BigDecimal usdValue) { this.usdValue = usdValue; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
