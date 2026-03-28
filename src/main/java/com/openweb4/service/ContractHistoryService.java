package com.openweb4.service;

import com.openweb4.model.ContractDataDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ContractHistoryService {

    private static final int MAX_RECORDS = 60;
    private static final long MAX_AGE_MS = 2 * 60 * 60 * 1000L; // 2 hours

    // symbol (BTC/ETH) -> list of snapshots
    private final Map<String, CopyOnWriteArrayList<Snapshot>> history = new ConcurrentHashMap<>();

    public static class Snapshot {
        public final long timestamp;
        public final double longShortRatio;
        public final double longAccount;
        public final double shortAccount;

        public Snapshot(long timestamp, double longShortRatio, double longAccount, double shortAccount) {
            this.timestamp = timestamp;
            this.longShortRatio = longShortRatio;
            this.longAccount = longAccount;
            this.shortAccount = shortAccount;
        }
    }

    private String normalizeSymbol(String symbol) {
        String s = symbol.toUpperCase();
        if (s.endsWith("USDT")) s = s.substring(0, s.length() - 4);
        return s;
    }

    public void record(String symbol, ContractDataDto dto) {
        if (dto == null || dto.getError() != null) return;
        String key = normalizeSymbol(symbol);
        history.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        CopyOnWriteArrayList<Snapshot> list = history.get(key);

        Snapshot snap = new Snapshot(
            dto.getTimestamp(),
            dto.getLongShortRatio(),
            dto.getLongAccount(),
            dto.getShortAccount()
        );
        list.add(snap);

        // Trim: remove old entries beyond MAX_AGE or MAX_RECORDS
        long cutoff = System.currentTimeMillis() - MAX_AGE_MS;
        list.removeIf(s -> s.timestamp < cutoff);
        while (list.size() > MAX_RECORDS) {
            list.remove(0);
        }
    }

    public List<Snapshot> getHistory(String symbol) {
        String key = normalizeSymbol(symbol);
        CopyOnWriteArrayList<Snapshot> list = history.get(key);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }
}
