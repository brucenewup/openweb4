package com.openweb4.model;

import java.math.BigDecimal;

public class HistoricalPricePoint {
    private String label;
    private BigDecimal price;

    public HistoricalPricePoint() {
    }

    public HistoricalPricePoint(String label, BigDecimal price) {
        this.label = label;
        this.price = price;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
