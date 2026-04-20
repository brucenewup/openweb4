package com.openweb4.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openweb4.model.AiAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for AI Agent trading activity monitoring
 */
@Service
public class AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);
    private static final int CACHE_MINUTES = 10;

    private final Cache<String, List<AiAgent>> cache;

    public AiAgentService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
    }

    public List<AiAgent> getAiAgents() {
        String cacheKey = "ai_agents";
        List<AiAgent> cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            List<AiAgent> agents = fetchAiAgents();
            if (!agents.isEmpty()) {
                cache.put(cacheKey, agents);
            }
            return agents;
        } catch (Exception e) {
            log.warn("Failed to fetch AI agents: {}", e.getMessage());
            return getFallbackAgents();
        }
    }

    private List<AiAgent> fetchAiAgents() {
        // TODO: Integrate with real APIs (Dune Analytics, DefiLlama, protocol APIs)
        // For now, return curated static data
        return getFallbackAgents();
    }

    private List<AiAgent> getFallbackAgents() {
        List<AiAgent> agents = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        agents.add(new AiAgent(
                "hermes",
                "Hermes Agent",
                "Hermes",
                new BigDecimal("15200000"),
                89.7,
                "Multi-Platform AI Assistant",
                true,
                "https://github.com/OpenAgentsInc/openagents",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "openclaw",
                "OpenClaw",
                "OpenClaw",
                new BigDecimal("12500000"),
                87.5,
                "Natural Language Trading",
                true,
                "https://openclaw.ai",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "virtuals-protocol",
                "Virtuals Protocol",
                "Virtuals",
                new BigDecimal("8900000"),
                82.3,
                "Agent Economy",
                true,
                "https://virtuals.io",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "elizaos",
                "ElizaOS",
                "ElizaOS",
                new BigDecimal("6700000"),
                79.8,
                "Open Framework",
                false,
                "https://elizaos.ai",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "clanker",
                "CLANKER",
                "CLANKER",
                new BigDecimal("4200000"),
                91.2,
                "Auto Token Launch",
                false,
                "https://clanker.world",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "polystrat",
                "PolyStrat",
                "PolyStrat",
                new BigDecimal("5800000"),
                85.6,
                "Prediction Markets",
                true,
                "https://polystrat.ai",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "autonolas",
                "Autonolas",
                "Autonolas",
                new BigDecimal("3500000"),
                76.4,
                "Autonomous Services",
                false,
                "https://autonolas.network",
                "Manual Curation",
                now
        ));

        agents.add(new AiAgent(
                "numerai",
                "Numerai",
                "Numerai",
                new BigDecimal("2900000"),
                73.8,
                "Crowdsourced Predictions",
                false,
                "https://numer.ai",
                "Manual Curation",
                now
        ));

        return agents;
    }
}
