package com.openweb4.service;

import com.openweb4.model.ExchangeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@Service
public class ExchangeCapabilityService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeCapabilityService.class);
    private static final String YML_PATH = "data/exchange-capabilities.yml";

    private final Map<String, List<ExchangeCapability>> capabilitiesByLang = new HashMap<>();
    private boolean loaded = false;

    @PostConstruct
    public void init() {
        loadData();
    }

    private void loadData() {
        if (loaded) return;
        try {
            Yaml yaml = new Yaml();
            InputStream is = getClass().getClassLoader().getResourceAsStream(YML_PATH);
            if (is == null) {
                log.warn("Exchange capabilities YAML not found at {}", YML_PATH);
                loadHardcodedData();
                return;
            }

            Map<String, Object> data = yaml.load(is);
            is.close();

            if (data == null || !data.containsKey("exchanges")) {
                log.warn("Invalid exchange capabilities YAML format");
                loadHardcodedData();
                return;
            }

            List<Map<String, Object>> exchanges = (List<Map<String, Object>>) data.get("exchanges");
            List<ExchangeCapability> enList = new ArrayList<>();
            List<ExchangeCapability> zhList = new ArrayList<>();

            for (Map<String, Object> ex : exchanges) {
                ExchangeCapability cap = new ExchangeCapability();
                cap.setExchange(getString(ex, "exchange"));
                cap.setOfficialSkill(getString(ex, "officialSkill"));
                cap.setLink(getString(ex, "link"));
                cap.setInstallDifficulty(getString(ex, "installDifficulty"));
                cap.setStrengths(getString(ex, "strengths"));
                cap.setSecurity(getString(ex, "security"));
                cap.setScope(getString(ex, "scope"));
                cap.setScore(getInt(ex, "score", 5));

                enList.add(cap);

                ExchangeCapability capZh = new ExchangeCapability();
                capZh.setExchange(translateExchangeName(cap.getExchange()));
                capZh.setOfficialSkill(translateSkillName(cap.getOfficialSkill()));
                capZh.setLink(cap.getLink());
                capZh.setInstallDifficulty(translateDifficulty(cap.getInstallDifficulty()));
                capZh.setStrengths(cap.getStrengths());
                capZh.setSecurity(cap.getSecurity());
                capZh.setScope(cap.getScope());
                capZh.setScore(cap.getScore());
                zhList.add(capZh);
            }

            capabilitiesByLang.put("en", enList);
            capabilitiesByLang.put("zh", zhList);
            loaded = true;
            log.info("Loaded exchange capabilities for {} exchanges", enList.size());

        } catch (Exception e) {
            log.error("Failed to load exchange capabilities: {}", e.getMessage());
            loadHardcodedData();
        }
    }

    private void loadHardcodedData() {
        List<ExchangeCapability> enList = new ArrayList<>();
        List<ExchangeCapability> zhList = new ArrayList<>();

        String[][] exchanges = {
                {"Binance", "Binance Smart Chain Skills", "Low", "Low fees, high liquidity, extensive trading pairs", "Advanced risk control, SAFU fund", "Spot, Futures, NFT, DeFi, Staking", "9"},
                {"Coinbase", "Coinbase Commerce", "Medium", "Regulatory compliant, secure, user-friendly", "Bank-grade security, insurance coverage", "Spot, Custody, Developer APIs", "8"},
                {"Kraken", "Kraken Pro Skills", "Medium", "Strong security, good liquidity, fiat support", "Proof of reserves, cold storage", "Spot, Futures, Margin", "8"},
                {"OKX", "OKX Trading Skills", "Low", "Comprehensive features, competitive fees", "Multi-tier security system", "Spot, Futures, Options, DeFi", "8"},
                {"Bybit", "Bybit Trading Bot", "Low", "Derivatives focus, fast execution, good API", "300M protection fund", "Spot, Derivatives, NFT", "7"},
                {"Bitget", "Bitget Trading Signals", "Low", "Copy trading, low fees, good API", "400M protection fund", "Spot, Futures, Copy Trading", "7"}
        };

        String[] zhExchanges = {"币安", "Coinbase", "Kraken", "OKX", "Bybit", "Bitget"};
        String[] zhDifficulty = {"低", "中", "低", "低", "低", "低"};

        for (int i = 0; i < exchanges.length; i++) {
            String[] e = exchanges[i];
            ExchangeCapability cap = new ExchangeCapability();
            cap.setExchange(e[0]);
            cap.setOfficialSkill(e[1]);
            cap.setLink("https://skill.ox.ooo/" + e[0].toLowerCase());
            cap.setInstallDifficulty(e[2]);
            cap.setStrengths(e[3]);
            cap.setSecurity(e[4]);
            cap.setScope(e[5]);
            cap.setScore(Integer.parseInt(e[6]));
            enList.add(cap);

            ExchangeCapability capZh = new ExchangeCapability();
            capZh.setExchange(zhExchanges[i]);
            capZh.setOfficialSkill(e[1]);
            capZh.setLink(cap.getLink());
            capZh.setInstallDifficulty(zhDifficulty[i]);
            capZh.setStrengths(e[3]);
            capZh.setSecurity(e[4]);
            capZh.setScope(e[5]);
            capZh.setScore(cap.getScore());
            zhList.add(capZh);
        }

        capabilitiesByLang.put("en", enList);
        capabilitiesByLang.put("zh", zhList);
        loaded = true;
    }

    public List<ExchangeCapability> getCapabilities(String lang) {
        loadData();
        String key = lang != null && lang.startsWith("zh") ? "zh" : "en";
        List<ExchangeCapability> caps = capabilitiesByLang.get(key);
        if (caps == null) {
            caps = capabilitiesByLang.get("en");
        }
        return caps != null ? caps : new ArrayList<>();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val != null) {
            try {
                return Integer.parseInt(val.toString());
            } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    private String translateExchangeName(String name) {
        switch (name) {
            case "Binance": return "币安";
            case "Coinbase": return "Coinbase";
            case "Kraken": return "Kraken";
            case "OKX": return "OKX";
            case "Bybit": return "Bybit";
            case "Bitget": return "Bitget";
            default: return name;
        }
    }

    private String translateSkillName(String name) {
        if (name.contains("Binance")) return "币安智能链 Skills";
        if (name.contains("Coinbase")) return "Coinbase Commerce";
        if (name.contains("Kraken")) return "Kraken Pro Skills";
        if (name.contains("OKX")) return "OKX 交易 Skills";
        if (name.contains("Bybit")) return "Bybit 交易机器人";
        if (name.contains("Bitget")) return "Bitget 交易信号";
        return name;
    }

    private String translateDifficulty(String difficulty) {
        switch (difficulty) {
            case "Low": return "低";
            case "Medium": return "中";
            case "High": return "高";
            default: return difficulty;
        }
    }
}
