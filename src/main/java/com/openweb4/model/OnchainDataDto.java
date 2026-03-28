package com.openweb4.model;

import java.math.BigDecimal;
import java.util.List;

public class OnchainDataDto {

    private String symbol;           // BTC / ETH / SOL
    private BigDecimal price;        // current price USD
    private long activeAddresses;    // 24h active addresses
    private BigDecimal networkHashrate; // TH/s (BTC) or GH/s (ETH)
    private BigDecimal transactionVolume24h; // USD
    private BigDecimal avgTransactionFee;   // USD
    private BigDecimal largeTransactionCount; // >$1M txs in 24h
    private BigDecimal inflowUsd24h;   // exchange inflow USD
    private BigDecimal outflowUsd24h;  // exchange outflow USD
    private BigDecimal netFlowUsd24h;  // inflow - outflow
    private List<FlowPoint> hourlyFlow; // 24h hourly flow chart data
    private List<LargeTx> recentLargeTxs; // large transactions
    private String dataSource;
    private long updatedAt;

    public OnchainDataDto() {}

    // ── Nested: hourly flow point ──
    public static class FlowPoint {
        private String hour;       // "00", "01" ... "23"
        private BigDecimal inflow;
        private BigDecimal outflow;
        public FlowPoint(String hour, BigDecimal inflow, BigDecimal outflow) {
            this.hour = hour; this.inflow = inflow; this.outflow = outflow;
        }
        public String getHour() { return hour; }
        public BigDecimal getInflow() { return inflow; }
        public BigDecimal getOutflow() { return outflow; }
    }

    // ── Nested: large transaction ──
    public static class LargeTx {
        private String hash;
        private String symbol;
        private BigDecimal amount;
        private BigDecimal usdValue;
        private String fromTag;  // "Unknown Wallet" / "Binance" etc
        private String toTag;
        private String timeAgo; // "5 min ago"
        public LargeTx(String hash, String symbol, BigDecimal amount, BigDecimal usdValue,
                       String fromTag, String toTag, String timeAgo) {
            this.hash = hash; this.symbol = symbol; this.amount = amount;
            this.usdValue = usdValue; this.fromTag = fromTag;
            this.toTag = toTag; this.timeAgo = timeAgo;
        }
        public String getHash() { return hash; }
        public String getSymbol() { return symbol; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getUsdValue() { return usdValue; }
        public String getFromTag() { return fromTag; }
        public String getToTag() { return toTag; }
        public String getTimeAgo() { return timeAgo; }
    }

    // ── Getters / Setters ──
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public long getActiveAddresses() { return activeAddresses; }
    public void setActiveAddresses(long activeAddresses) { this.activeAddresses = activeAddresses; }
    public BigDecimal getNetworkHashrate() { return networkHashrate; }
    public void setNetworkHashrate(BigDecimal networkHashrate) { this.networkHashrate = networkHashrate; }
    public BigDecimal getTransactionVolume24h() { return transactionVolume24h; }
    public void setTransactionVolume24h(BigDecimal transactionVolume24h) { this.transactionVolume24h = transactionVolume24h; }
    public BigDecimal getAvgTransactionFee() { return avgTransactionFee; }
    public void setAvgTransactionFee(BigDecimal avgTransactionFee) { this.avgTransactionFee = avgTransactionFee; }
    public BigDecimal getLargeTransactionCount() { return largeTransactionCount; }
    public void setLargeTransactionCount(BigDecimal largeTransactionCount) { this.largeTransactionCount = largeTransactionCount; }
    public BigDecimal getInflowUsd24h() { return inflowUsd24h; }
    public void setInflowUsd24h(BigDecimal inflowUsd24h) { this.inflowUsd24h = inflowUsd24h; }
    public BigDecimal getOutflowUsd24h() { return outflowUsd24h; }
    public void setOutflowUsd24h(BigDecimal outflowUsd24h) { this.outflowUsd24h = outflowUsd24h; }
    public BigDecimal getNetFlowUsd24h() { return netFlowUsd24h; }
    public void setNetFlowUsd24h(BigDecimal netFlowUsd24h) { this.netFlowUsd24h = netFlowUsd24h; }
    public List<FlowPoint> getHourlyFlow() { return hourlyFlow; }
    public void setHourlyFlow(List<FlowPoint> hourlyFlow) { this.hourlyFlow = hourlyFlow; }
    public List<LargeTx> getRecentLargeTxs() { return recentLargeTxs; }
    public void setRecentLargeTxs(List<LargeTx> recentLargeTxs) { this.recentLargeTxs = recentLargeTxs; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
