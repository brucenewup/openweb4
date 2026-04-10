package com.openweb4.service;

import org.yaml.snakeyaml.Yaml;
import com.openweb4.model.AuditStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads smart contract audit status from YAML. MVP implementation; future: replace with API.
 */
@Service
public class AuditStatusService {

    private static final Logger log = LoggerFactory.getLogger(AuditStatusService.class);
    private static final String DATA_FILE = "data/audit-status.yml";

    private final List<AuditStatus> audits = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadData();
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        try {
            Path path = Paths.get("src/main/resources", DATA_FILE);
            if (!Files.exists(path)) {
                log.warn("Audit status YAML not found at {}, using empty data", path);
                return;
            }
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(Files.newInputStream(path));

            List<Map<String, Object>> auditList = (List<Map<String, Object>>) root.get("audits");
            if (auditList == null) return;

            for (Map<String, Object> entry : auditList) {
                AuditStatus a = new AuditStatus();
                a.setContractName((String) entry.get("contractName"));
                a.setContractAddress((String) entry.get("contractAddress"));
                a.setProject((String) entry.get("project"));
                a.setAuditor((String) entry.get("auditor"));
                a.setAuditDate((String) entry.get("auditDate"));
                a.setReportUrl((String) entry.get("reportUrl"));
                a.setRiskLevel((String) entry.get("riskLevel"));
                a.setNotes((String) entry.get("notes"));

                Map<String, Object> findingsRaw = (Map<String, Object>) entry.get("findings");
                if (findingsRaw != null) {
                    AuditStatus.Findings f = new AuditStatus.Findings();
                    f.setCritical(intVal(findingsRaw.get("critical")));
                    f.setHigh(intVal(findingsRaw.get("high")));
                    f.setMedium(intVal(findingsRaw.get("medium")));
                    f.setLow(intVal(findingsRaw.get("low")));
                    f.setInformational(intVal(findingsRaw.get("informational")));
                    a.setFindings(f);
                }

                audits.add(a);
            }

            log.info("Loaded {} audit entries from YAML", audits.size());
        } catch (Exception e) {
            log.warn("Failed to load audit status YAML: {}", e.getMessage());
        }
    }

    /**
     * Returns all audits with localized labels.
     * @param locale "zh" or "en"
     */
    public List<Map<String, Object>> getAllAudits(String locale) {
        return audits.stream()
                .sorted(Comparator.comparing(AuditStatus::getProject))
                .map(a -> toMap(a, locale))
                .collect(Collectors.toList());
    }

    /**
     * Returns audits grouped by risk level.
     */
    public Map<String, List<Map<String, Object>>> getGroupedByRisk(String locale) {
        return audits.stream()
                .sorted(Comparator.comparing(AuditStatus::getProject))
                .map(a -> toMap(a, locale))
                .collect(Collectors.groupingBy(m -> (String) m.get("riskLevel")));
    }

    private Map<String, Object> toMap(AuditStatus a, String locale) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("contractName", a.getContractName());
        map.put("contractAddress", a.getContractAddress());
        map.put("project", a.getProject());
        map.put("auditor", a.getAuditor());
        map.put("auditDate", a.getAuditDate());
        map.put("reportUrl", a.getReportUrl());
        map.put("riskLevel", a.getRiskLevel());
        map.put("notes", a.getNotes());

        if (a.getFindings() != null) {
            LinkedHashMap<String, Object> f = new LinkedHashMap<>();
            f.put("critical", a.getFindings().getCritical());
            f.put("high", a.getFindings().getHigh());
            f.put("medium", a.getFindings().getMedium());
            f.put("low", a.getFindings().getLow());
            f.put("informational", a.getFindings().getInformational());
            map.put("findings", f);
        }

        if ("zh".equalsIgnoreCase(locale)) {
            map.put("riskLabel", riskLabelZh(a.getRiskLevel()));
            map.put("projectLabel", a.getProject() + " · " + a.getContractName());
        } else {
            map.put("riskLabel", a.getRiskLevel());
            map.put("projectLabel", a.getContractName() + " (" + a.getProject() + ")");
        }

        return map;
    }

    private static String riskLabelZh(String riskLevel) {
        if (riskLevel == null) return "未知";
        return switch (riskLevel.toLowerCase()) {
            case "low" -> "低风险";
            case "medium" -> "中风险";
            case "high" -> "高风险";
            default -> riskLevel;
        };
    }

    private static int intVal(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString().trim()); } catch (Exception e) { return 0; }
    }
}
