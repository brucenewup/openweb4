package com.openweb4.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Real-World Asset (RWA) tokenization project model
 */
public class RwaProject {
    private String id;
    private String name;
    private String protocol;
    private BigDecimal tvl;
    private String collateralType;
    private Integer transparencyScore;
    private Boolean hasProofOfPortfolio;
    private String website;
    private String dataSource;
    private LocalDateTime updatedAt;

    public RwaProject() {}

    public RwaProject(String id, String name, String protocol, BigDecimal tvl, 
                      String collateralType, Integer transparencyScore, 
                      Boolean hasProofOfPortfolio, String website, 
                      String dataSource, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.tvl = tvl;
        this.collateralType = collateralType;
        this.transparencyScore = transparencyScore;
        this.hasProofOfPortfolio = hasProofOfPortfolio;
        this.website = website;
        this.dataSource = dataSource;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public BigDecimal getTvl() { return tvl; }
    public void setTvl(BigDecimal tvl) { this.tvl = tvl; }
    
    public String getCollateralType() { return collateralType; }
    public void setCollateralType(String collateralType) { this.collateralType = collateralType; }
    
    public Integer getTransparencyScore() { return transparencyScore; }
    public void setTransparencyScore(Integer transparencyScore) { this.transparencyScore = transparencyScore; }
    
    public Boolean getHasProofOfPortfolio() { return hasProofOfPortfolio; }
    public void setHasProofOfPortfolio(Boolean hasProofOfPortfolio) { this.hasProofOfPortfolio = hasProofOfPortfolio; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
