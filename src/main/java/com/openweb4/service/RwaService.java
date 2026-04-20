package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openweb4.model.RwaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for Real-World Asset (RWA) tokenization projects
 */
@Service
public class RwaService {

    private static final Logger log = LoggerFactory.getLogger(RwaService.class);
    private static final int CACHE_MINUTES = 10;

    private final Cache<String, List<RwaProject>> cache;

    public RwaService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }

    public List<RwaProject> getRwaProjects() {
        String cacheKey = "rwa_projects";
        List<RwaProject> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            List<RwaProject> projects = fetchRwaProjects();
            if (!projects.isEmpty()) {
                cache.put(cacheKey, projects);
            }
            return projects;
        } catch (Exception e) {
            log.warn("Failed to fetch RWA projects: {}", e.getMessage());
            return getFallbackProjects();
        }
    }

    private List<RwaProject> fetchRwaProjects() {
        // TODO: Integrate with real APIs (Centrifuge, MakerDAO, Ondo Finance)
        // For now, return curated static data
        return getFallbackProjects();
    }

    private List<RwaProject> getFallbackProjects() {
        List<RwaProject> projects = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        projects.add(new RwaProject(
                "centrifuge",
                "Centrifuge",
                "Centrifuge",
                new BigDecimal("450000000"),
                "Real Estate, Invoices, Trade Finance",
                95,
                true,
                "https://centrifuge.io",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "makerdao-rwa",
                "MakerDAO RWA",
                "MakerDAO",
                new BigDecimal("1200000000"),
                "US Treasuries, Corporate Bonds",
                98,
                true,
                "https://makerdao.com",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "ondo-finance",
                "Ondo Finance",
                "Ondo",
                new BigDecimal("580000000"),
                "US Treasuries, Money Market Funds",
                92,
                false,
                "https://ondo.finance",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "goldfinch",
                "Goldfinch",
                "Goldfinch",
                new BigDecimal("120000000"),
                "Emerging Market Loans",
                88,
                false,
                "https://goldfinch.finance",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "maple-finance",
                "Maple Finance",
                "Maple",
                new BigDecimal("350000000"),
                "Corporate Credit, Crypto Loans",
                90,
                true,
                "https://maple.finance",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "backed-finance",
                "Backed Finance",
                "Backed",
                new BigDecimal("180000000"),
                "Equities, Bonds",
                85,
                false,
                "https://backed.fi",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "swarm-markets",
                "Swarm Markets",
                "Swarm",
                new BigDecimal("95000000"),
                "Real Estate, Private Equity",
                82,
                false,
                "https://swarm.markets",
                "Manual Curation",
                now
        ));

        projects.add(new RwaProject(
                "realT",
                "RealT",
                "RealT",
                new BigDecimal("75000000"),
                "US Real Estate",
                80,
                false,
                "https://realt.co",
                "Manual Curation",
                now
        ));

        return projects;
    }
}
