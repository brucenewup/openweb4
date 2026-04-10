package com.openweb4.model;

import java.math.BigDecimal;

/**
 * Smart contract audit status entry.
 */
public class AuditStatus {

    private String contractName;
    private String contractAddress;
    private String project;
    private String auditor;
    private String auditDate;
    private String reportUrl;
    private String riskLevel;
    private Findings findings;
    private String notes;

    public static class Findings {
        private int critical;
        private int high;
        private int medium;
        private int low;
        private int informational;

        public Findings() {}

        public Findings(int critical, int high, int medium, int low, int informational) {
            this.critical = critical;
            this.high = high;
            this.medium = medium;
            this.low = low;
            this.informational = informational;
        }

        public int getCritical() { return critical; }
        public void setCritical(int critical) { this.critical = critical; }
        public int getHigh() { return high; }
        public void setHigh(int high) { this.high = high; }
        public int getMedium() { return medium; }
        public void setMedium(int medium) { this.medium = medium; }
        public int getLow() { return low; }
        public void setLow(int low) { this.low = low; }
        public int getInformational() { return informational; }
        public void setInformational(int informational) { this.informational = informational; }
    }

    public AuditStatus() {}

    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public String getAuditDate() { return auditDate; }
    public void setAuditDate(String auditDate) { this.auditDate = auditDate; }
    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Findings getFindings() { return findings; }
    public void setFindings(Findings findings) { this.findings = findings; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
