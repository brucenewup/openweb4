package com.openweb4.model;

import java.math.BigDecimal;

public class ForecastPoint {
    private int year;
    private BigDecimal conservative;
    private BigDecimal base;
    private BigDecimal bullish;
    private BigDecimal fairValue;
    private String commentary;

    public ForecastPoint() {
    }

    public ForecastPoint(int year, BigDecimal conservative, BigDecimal base, BigDecimal bullish,
                         BigDecimal fairValue, String commentary) {
        this.year = year;
        this.conservative = conservative;
        this.base = base;
        this.bullish = bullish;
        this.fairValue = fairValue;
        this.commentary = commentary;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getConservative() {
        return conservative;
    }

    public void setConservative(BigDecimal conservative) {
        this.conservative = conservative;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getBullish() {
        return bullish;
    }

    public void setBullish(BigDecimal bullish) {
        this.bullish = bullish;
    }

    public BigDecimal getFairValue() {
        return fairValue;
    }

    public void setFairValue(BigDecimal fairValue) {
        this.fairValue = fairValue;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }
}
