package com.openweb4.model;

public class ContractDataDto {
    private String symbol;
    private double longShortRatio;    // 多空账户比
    private double longAccount;       // 多仓账户占比 %
    private double shortAccount;      // 空仓账户占比 %
    private double openInterest;      // 未平仓合约量 (USDT)
    private double fundingRate;       // 资金费率
    private double takerBuyRatio;     // 主动买量比
    private double takerSellRatio;    // 主动卖量比
    private long timestamp;
    private String error;

    public ContractDataDto() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getLongShortRatio() { return longShortRatio; }
    public void setLongShortRatio(double longShortRatio) { this.longShortRatio = longShortRatio; }

    public double getLongAccount() { return longAccount; }
    public void setLongAccount(double longAccount) { this.longAccount = longAccount; }

    public double getShortAccount() { return shortAccount; }
    public void setShortAccount(double shortAccount) { this.shortAccount = shortAccount; }

    public double getOpenInterest() { return openInterest; }
    public void setOpenInterest(double openInterest) { this.openInterest = openInterest; }

    public double getFundingRate() { return fundingRate; }
    public void setFundingRate(double fundingRate) { this.fundingRate = fundingRate; }

    public double getTakerBuyRatio() { return takerBuyRatio; }
    public void setTakerBuyRatio(double takerBuyRatio) { this.takerBuyRatio = takerBuyRatio; }

    public double getTakerSellRatio() { return takerSellRatio; }
    public void setTakerSellRatio(double takerSellRatio) { this.takerSellRatio = takerSellRatio; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
