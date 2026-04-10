package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Token unlock event entry.
 */
public class TokenUnlock {

    private String token;
    private String name;
    private BigDecimal totalSupply;
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String event;

    public TokenUnlock() {}

    public TokenUnlock(String token, String name, BigDecimal totalSupply,
            LocalDate date, BigDecimal amount, BigDecimal percentage, String event) {
        this.token = token;
        this.name = name;
        this.totalSupply = totalSupply;
        this.date = date;
        this.amount = amount;
        this.percentage = percentage;
        this.event = event;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getTotalSupply() { return totalSupply; }
    public void setTotalSupply(BigDecimal totalSupply) { this.totalSupply = totalSupply; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
}
