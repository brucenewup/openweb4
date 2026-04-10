package com.openweb4.model;

/**
 * 交易所能力对比模型
 */
public class ExchangeCapability {
    private String exchange;
    private String officialSkill;
    private String link;
    private String installDifficulty;
    private String strengths;
    private String security;
    private String scope;
    private int score; // 1-10

    public ExchangeCapability() {}

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getOfficialSkill() { return officialSkill; }
    public void setOfficialSkill(String officialSkill) { this.officialSkill = officialSkill; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getInstallDifficulty() { return installDifficulty; }
    public void setInstallDifficulty(String installDifficulty) { this.installDifficulty = installDifficulty; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getSecurity() { return security; }
    public void setSecurity(String security) { this.security = security; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
