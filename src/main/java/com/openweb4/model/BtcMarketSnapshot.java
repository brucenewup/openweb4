package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BtcMarketSnapshot {
    private BigDecimal currentPrice;
    private BigDecimal athPrice;
    private BigDecimal ma200;
    private BigDecimal twoYearMa;
    private BigDecimal powerLawFairValue;
    private BigDecimal currentVsPowerLaw;
    private BigDecimal currentVs200Ma;
    private BigDecimal drawdownFromAth;
    private BigDecimal fearGreedValue;
    private String fearGreedLabel;
    private String forecastMethod;
    private String dataSource;
    private LocalDateTime updatedAt;
    private List<MarketIndex> indices = new ArrayList<>();
    private List<ForecastPoint> forecasts = new ArrayList<>();
    private List<HistoricalPricePoint> yearlyHistory = new ArrayList<>();
    private List<String> forecastNotes = new ArrayList<>();

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getAthPrice() {
        return athPrice;
    }

    public void setAthPrice(BigDecimal athPrice) {
        this.athPrice = athPrice;
    }

    public BigDecimal getMa200() {
        return ma200;
    }

    public void setMa200(BigDecimal ma200) {
        this.ma200 = ma200;
    }

    public BigDecimal getTwoYearMa() {
        return twoYearMa;
    }

    public void setTwoYearMa(BigDecimal twoYearMa) {
        this.twoYearMa = twoYearMa;
    }

    public BigDecimal getPowerLawFairValue() {
        return powerLawFairValue;
    }

    public void setPowerLawFairValue(BigDecimal powerLawFairValue) {
        this.powerLawFairValue = powerLawFairValue;
    }

    public BigDecimal getCurrentVsPowerLaw() {
        return currentVsPowerLaw;
    }

    public void setCurrentVsPowerLaw(BigDecimal currentVsPowerLaw) {
        this.currentVsPowerLaw = currentVsPowerLaw;
    }

    public BigDecimal getCurrentVs200Ma() {
        return currentVs200Ma;
    }

    public void setCurrentVs200Ma(BigDecimal currentVs200Ma) {
        this.currentVs200Ma = currentVs200Ma;
    }

    public BigDecimal getDrawdownFromAth() {
        return drawdownFromAth;
    }

    public void setDrawdownFromAth(BigDecimal drawdownFromAth) {
        this.drawdownFromAth = drawdownFromAth;
    }

    public BigDecimal getFearGreedValue() {
        return fearGreedValue;
    }

    public void setFearGreedValue(BigDecimal fearGreedValue) {
        this.fearGreedValue = fearGreedValue;
    }

    public String getFearGreedLabel() {
        return fearGreedLabel;
    }

    public void setFearGreedLabel(String fearGreedLabel) {
        this.fearGreedLabel = fearGreedLabel;
    }

    public String getForecastMethod() {
        return forecastMethod;
    }

    public void setForecastMethod(String forecastMethod) {
        this.forecastMethod = forecastMethod;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MarketIndex> getIndices() {
        return indices;
    }

    public void setIndices(List<MarketIndex> indices) {
        this.indices = indices;
    }

    public List<ForecastPoint> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<ForecastPoint> forecasts) {
        this.forecasts = forecasts;
    }

    public List<HistoricalPricePoint> getYearlyHistory() {
        return yearlyHistory;
    }

    public void setYearlyHistory(List<HistoricalPricePoint> yearlyHistory) {
        this.yearlyHistory = yearlyHistory;
    }

    public List<String> getForecastNotes() {
        return forecastNotes;
    }

    public void setForecastNotes(List<String> forecastNotes) {
        this.forecastNotes = forecastNotes;
    }
}
