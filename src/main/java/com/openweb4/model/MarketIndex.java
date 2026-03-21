package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketIndex {
    private String key;
    private String name;
    private String valueLabel;
    private BigDecimal numericValue;
    private String unit;
    private String signal;
    private String tone;
    private String description;
    private String formula;
    private String source;
    private LocalDateTime updatedAt;

    public MarketIndex() {
    }

    public MarketIndex(String key, String name, String valueLabel, BigDecimal numericValue, String unit,
                       String signal, String tone, String description, String formula, String source,
                       LocalDateTime updatedAt) {
        this.key = key;
        this.name = name;
        this.valueLabel = valueLabel;
        this.numericValue = numericValue;
        this.unit = unit;
        this.signal = signal;
        this.tone = tone;
        this.description = description;
        this.formula = formula;
        this.source = source;
        this.updatedAt = updatedAt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueLabel() {
        return valueLabel;
    }

    public void setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(BigDecimal numericValue) {
        this.numericValue = numericValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
