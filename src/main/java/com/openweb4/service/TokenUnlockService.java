package com.openweb4.service;

import org.yaml.snakeyaml.Yaml;
import com.openweb4.model.TokenUnlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Loads token unlock data from YAML. MVP implementation; future: replace with API source.
 */
@Service
public class TokenUnlockService {

    private static final Logger log = LoggerFactory.getLogger(TokenUnlockService.class);
    private static final String DATA_FILE = "data/token-unlocks.yml";

    private final List<TokenUnlock> allUnlocks = new ArrayList<>();
    private final Map<String, List<TokenUnlock>> byToken = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadData();
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        try {
            Path path = Paths.get("src/main/resources", DATA_FILE);
            if (!Files.exists(path)) {
                log.warn("Token unlock YAML not found at {}, using empty data", path);
                return;
            }
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(Files.newInputStream(path));

            List<Map<String, Object>> unlocks = (List<Map<String, Object>>) root.get("unlocks");
            if (unlocks == null) return;

            for (Map<String, Object> entry : unlocks) {
                String token = (String) entry.get("token");
                String name = (String) entry.get("name");
                BigDecimal totalSupply = toBd(entry.get("totalSupply"));

                List<Map<String, Object>> schedule = (List<Map<String, Object>>) entry.get("unlockSchedule");
                if (schedule == null) continue;

                for (Map<String, Object> sched : schedule) {
                    String dateStr = (String) sched.get("date");
                    LocalDate date = LocalDate.parse(dateStr);
                    BigDecimal amount = toBd(sched.get("amount"));
                    BigDecimal percentage = toBd(sched.get("percentage"));
                    String event = (String) sched.get("event");

                    TokenUnlock unlock = new TokenUnlock(token, name, totalSupply, date, amount, percentage, event);
                    allUnlocks.add(unlock);
                    byToken.computeIfAbsent(token, k -> new ArrayList<>()).add(unlock);
                }
            }

            log.info("Loaded {} token unlock events from YAML", allUnlocks.size());
        } catch (Exception e) {
            log.warn("Failed to load token unlock YAML: {}", e.getMessage());
        }
    }

    /**
     * Returns upcoming unlocks sorted by date.
     * @param locale "zh" or "en"
     */
    public List<Map<String, Object>> getUpcomingUnlocks(String locale) {
        LocalDate today = LocalDate.now();
        List<TokenUnlock> upcoming = allUnlocks.stream()
                .filter(u -> !u.getDate().isBefore(today))
                .sorted(Comparator.comparing(TokenUnlock::getDate))
                .collect(Collectors.toList());

        return upcoming.stream().map(u -> toMap(u, locale)).collect(Collectors.toList());
    }

    /**
     * Returns all unlocks sorted by date.
     * @param locale "zh" or "en"
     */
    public List<Map<String, Object>> getAllUnlocks(String locale) {
        return allUnlocks.stream()
                .sorted(Comparator.comparing(TokenUnlock::getDate))
                .map(u -> toMap(u, locale))
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(TokenUnlock u, String locale) {
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("token", u.getToken());
        map.put("name", u.getName());
        map.put("totalSupply", u.getTotalSupply());
        map.put("date", u.getDate().toString());
        map.put("amount", u.getAmount());
        map.put("percentage", u.getPercentage());
        map.put("event", u.getEvent());

        if ("zh".equalsIgnoreCase(locale)) {
            map.put("tokenLabel", u.getToken() + " (" + u.getName() + ")");
            map.put("dateLabel", u.getDate().toString());
        } else {
            map.put("tokenLabel", u.getName() + " (" + u.getToken() + ")");
            map.put("dateLabel", u.getDate().toString());
        }

        // Days until unlock
        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), u.getDate());
        map.put("daysUntil", daysUntil);
        return map;
    }

    private static BigDecimal toBd(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return new BigDecimal(val.toString());
    }
}
