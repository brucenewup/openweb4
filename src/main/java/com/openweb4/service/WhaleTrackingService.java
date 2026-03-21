package com.openweb4.service;

import com.openweb4.model.WhaleTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class WhaleTrackingService {

    public List<WhaleTransaction> getRecentWhaleTransactions() {
        List<WhaleTransaction> transactions = new ArrayList<>();
        transactions.addAll(getBtcWhaleTransactions());
        transactions.addAll(getEthWhaleTransactions());
        transactions.sort(Comparator.comparing(WhaleTransaction::getTimestamp).reversed());
        return transactions;
    }

    private List<WhaleTransaction> getBtcWhaleTransactions() {
        List<WhaleTransaction> list = new ArrayList<>();
        list.add(new WhaleTransaction(
                "btc_tx_001", "BTC", new BigDecimal("150"), new BigDecimal("10500000"),
                "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh",
                "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                LocalDateTime.now().minusMinutes(5)));
        list.add(new WhaleTransaction(
                "btc_tx_002", "BTC", new BigDecimal("320"), new BigDecimal("22400000"),
                "bc1q9dpy5pmc6tmh7w3t7h5lxszv7k8lk5n5x7r3hj",
                "bc1qg9m0qs7mqgt464n6l2m0h5s4g7j5qx3vy3q3hq",
                LocalDateTime.now().minusMinutes(12)));
        list.add(new WhaleTransaction(
                "btc_tx_003", "BTC", new BigDecimal("85"), new BigDecimal("5950000"),
                "bc1qvzer0r9c8k3lkk3m5y5l0y9s7g8n3t0x7k5m6",
                "bc1qykj5s0h5s5h5j5j5j5j5j5j5j5j5j5j5j5j5",
                LocalDateTime.now().minusMinutes(25)));
        return list;
    }

    private List<WhaleTransaction> getEthWhaleTransactions() {
        List<WhaleTransaction> list = new ArrayList<>();
        list.add(new WhaleTransaction(
                "eth_tx_001", "ETH", new BigDecimal("5000"), new BigDecimal("17500000"),
                "0x28c6c06298d514db089934071355e5743bf21d60",
                "0x21a31ee1afc51d94c2efccaa2092ad1028285549",
                LocalDateTime.now().minusMinutes(8)));
        list.add(new WhaleTransaction(
                "eth_tx_002", "ETH", new BigDecimal("12000"), new BigDecimal("42000000"),
                "0x56d925f56f4f118a3d8c9a0c1c6e5f7a8e9c0b1d",
                "0x47ac0fb4f2d84898e4d9e7b4dab3c24507a6d503",
                LocalDateTime.now().minusMinutes(18)));
        list.add(new WhaleTransaction(
                "eth_tx_003", "ETH", new BigDecimal("2800"), new BigDecimal("9800000"),
                "0x3f5ce5fbfe3e9af3971dd833d26ba9b5c936f0be",
                "0xdfd5293d8e347dfe59e90efd55b2956a1343963d",
                LocalDateTime.now().minusMinutes(30)));
        return list;
    }

    public List<WhaleTransaction> getHistoricalFlowData() {
        List<WhaleTransaction> flows = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            flows.add(new WhaleTransaction(
                    "flow-in-" + i, "BTC",
                    new BigDecimal(String.valueOf(50 + i * 10L)),
                    new BigDecimal(String.valueOf(3500000 + i * 500000L)),
                    "inflow", "exchange",
                    LocalDateTime.now().minusHours(i)));
            flows.add(new WhaleTransaction(
                    "flow-out-" + i, "BTC",
                    new BigDecimal(String.valueOf(45 + i * 8L)),
                    new BigDecimal(String.valueOf(2800000 + i * 420000L)),
                    "outflow", "wallet",
                    LocalDateTime.now().minusHours(i)));
        }
        // ETH flow data
        for (int i = 23; i >= 0; i--) {
            flows.add(new WhaleTransaction(
                    "eth-flow-in-" + i, "ETH",
                    new BigDecimal(String.valueOf(800 + i * 120L)),
                    new BigDecimal(String.valueOf(2800000 + i * 420000L)),
                    "inflow", "exchange",
                    LocalDateTime.now().minusHours(i)));
            flows.add(new WhaleTransaction(
                    "eth-flow-out-" + i, "ETH",
                    new BigDecimal(String.valueOf(650 + i * 95L)),
                    new BigDecimal(String.valueOf(2300000 + i * 330000L)),
                    "outflow", "wallet",
                    LocalDateTime.now().minusHours(i)));
        }
        flows.sort(Comparator.comparing(WhaleTransaction::getTimestamp));
        return flows;
    }
}
