package com.openweb4.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openweb4.model.BtcMarketSnapshot;
import com.openweb4.model.CryptoPrice;
import com.openweb4.model.ForecastPoint;
import com.openweb4.model.HistoricalPricePoint;
import com.openweb4.model.MarketIndex;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BtcMarketService {

    private static final Logger log = LoggerFactory.getLogger(BtcMarketService.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final long CACHE_TTL_MILLIS = TimeUnit.MINUTES.toMillis(20);
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy", Locale.ROOT);
    private static final ZoneId UTC = ZoneId.of("UTC");

    private final okhttp3.OkHttpClient http;
    private final CryptoPriceService cryptoPriceService;

    private volatile BtcMarketSnapshot cachedSnapshot;
    private volatile long cachedAtMillis = 0L;

    public BtcMarketService(CryptoPriceService cryptoPriceService, okhttp3.OkHttpClient http) {
        this.cryptoPriceService = cryptoPriceService;
        this.http = http;
    }

    public BtcMarketSnapshot getSnapshot(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (!forceRefresh && cachedSnapshot != null && now - cachedAtMillis < CACHE_TTL_MILLIS) {
            return cachedSnapshot;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (!forceRefresh && cachedSnapshot != null && now - cachedAtMillis < CACHE_TTL_MILLIS) {
                return cachedSnapshot;
            }
            try {
                BtcMarketSnapshot snapshot = buildSnapshot();
                cachedSnapshot = snapshot;
                cachedAtMillis = now;
                return snapshot;
            } catch (Exception e) {
                log.warn("Failed to build BTC market snapshot: {}", e.getMessage(), e);
                if (cachedSnapshot != null) {
                    return cachedSnapshot;
                }
                return fallbackSnapshot();
            }
        }
    }

    private BtcMarketSnapshot buildSnapshot() throws IOException {
        List<DailyPoint> history = fetchDailyHistory();
        if (history.size() < 800) {
            throw new IOException("Insufficient BTC history points: " + history.size());
        }

        applyLiveSpotPrice(history);

        Stats stats = calculateStats(history);
        FearGreed fearGreed = fetchFearGreed();
        LocalDateTime updatedAt = LocalDateTime.now();

        BtcMarketSnapshot snapshot = new BtcMarketSnapshot();
        snapshot.setCurrentPrice(stats.currentPrice);
        snapshot.setAthPrice(stats.athPrice);
        snapshot.setMa200(stats.ma200);
        snapshot.setTwoYearMa(stats.ma730);
        snapshot.setPowerLawFairValue(stats.currentFairValue);
        snapshot.setCurrentVsPowerLaw(percent(stats.currentPrice.divide(stats.currentFairValue, 8, RoundingMode.HALF_UP)));
        snapshot.setCurrentVs200Ma(percent(stats.currentPrice.divide(stats.ma200, 8, RoundingMode.HALF_UP)));
        snapshot.setDrawdownFromAth(percent(BigDecimal.ONE.subtract(stats.currentPrice.divide(stats.athPrice, 8, RoundingMode.HALF_UP))));
        snapshot.setFearGreedValue(fearGreed.value);
        snapshot.setFearGreedLabel(fearGreed.label);
        snapshot.setForecastMethod("BTC 历史日线对数回归（Power Law） + 历史偏离分位数区间");
        snapshot.setDataSource("CryptoCompare 日线 + CoinGecko 实时现价 + Alternative.me 恐慌贪婪指数");
        snapshot.setUpdatedAt(updatedAt);
        snapshot.setIndices(buildIndices(stats, fearGreed, updatedAt));
        snapshot.setForecasts(buildForecasts(stats));
        snapshot.setYearlyHistory(buildYearlyHistory(history));
        snapshot.setForecastNotes(buildForecastNotes(stats));
        return snapshot;
    }

    private List<MarketIndex> buildIndices(Stats stats, FearGreed fearGreed, LocalDateTime updatedAt) {
        List<MarketIndex> out = new ArrayList<>();

        out.add(new MarketIndex(
                "fear-greed",
                "恐慌贪婪指数",
                fearGreed.value.setScale(0, RoundingMode.HALF_UP).toPlainString(),
                fearGreed.value,
                "/100",
                fearGreed.label,
                toneByFearGreed(fearGreed.value),
                "公开 API 直连，反映市场情绪冷热。数值越低，市场越恐慌。",
                "Alternative.me 官方值",
                "Alternative.me",
                updatedAt
        ));

        BigDecimal index999 = scaled(stats.currentPrice.divide(stats.ma200, 8, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        out.add(new MarketIndex(
                "index-999",
                "999 定投指数",
                index999.toPlainString(),
                index999,
                "",
                signalFor999(index999),
                toneFor999(index999),
                "用现价 / 200 日均线 × 100 近似“999 定投温度”，越低越接近价值区。",
                "(现价 ÷ 200DMA) × 100",
                "CryptoCompare + CoinGecko",
                updatedAt
        ));

        BigDecimal ahr999 = scaled(
                stats.currentPrice.divide(stats.ma200, 8, RoundingMode.HALF_UP)
                        .multiply(stats.currentPrice.divide(stats.currentFairValue, 8, RoundingMode.HALF_UP))
        );
        out.add(new MarketIndex(
                "ahr999",
                "AHR999（近似）",
                ahr999.toPlainString(),
                ahr999,
                "",
                signalForAhr999(ahr999),
                toneForAhr999(ahr999),
                "无法稳定抓到第三方原值时，使用公开历史数据近似复现：同时考虑 200 日成本与长期公允值偏离。",
                "(现价 ÷ 200DMA) × (现价 ÷ Power-Law 公允值)",
                "CryptoCompare + CoinGecko",
                updatedAt
        ));

        BigDecimal dipIndex = scaled(calculateDipIndex(stats, fearGreed));
        out.add(new MarketIndex(
                "dip-index",
                "抄底指数",
                dipIndex.toPlainString(),
                dipIndex,
                "/100",
                signalForDip(dipIndex),
                toneForDip(dipIndex),
                "综合恐慌程度、距历史高点回撤、以及相对 2 年均线的低估程度做成的公开数据派生分数。",
                "45% 回撤 + 35% 低估 + 20% 情绪",
                "CryptoCompare + CoinGecko + Alternative.me",
                updatedAt
        ));

        BigDecimal twoYearMultiplier = scaled(stats.currentPrice.divide(stats.ma730.multiply(new BigDecimal("5")), 8, RoundingMode.HALF_UP));
        out.add(new MarketIndex(
                "2y-ma-multiplier",
                "两年 MA 乘数",
                twoYearMultiplier.toPlainString(),
                twoYearMultiplier,
                "x",
                signalForTwoYearMultiplier(twoYearMultiplier),
                toneForTwoYearMultiplier(twoYearMultiplier),
                "观察 BTC 是否接近经典 2 年均线 × 5 的顶部风险带。数值越低，离过热区越远。",
                "现价 ÷ (2YMA × 5)",
                "CryptoCompare + CoinGecko",
                updatedAt
        ));

        BigDecimal piCycle = scaled(stats.ma111.divide(stats.ma350.multiply(new BigDecimal("2")), 8, RoundingMode.HALF_UP));
        out.add(new MarketIndex(
                "pi-cycle",
                "Pi Cycle 热度",
                piCycle.toPlainString(),
                piCycle,
                "x",
                signalForPiCycle(piCycle),
                toneForPiCycle(piCycle),
                "经典顶部监测指标。111DMA 接近或上穿 350DMA×2 时，通常意味着周期热度较高。",
                "111DMA ÷ (350DMA × 2)",
                "CryptoCompare",
                updatedAt
        ));

        return out;
    }

    private List<ForecastPoint> buildForecasts(Stats stats) {
        List<ForecastPoint> out = new ArrayList<>();
        int startYear = LocalDate.now().getYear();
        for (int year = startYear; year < startYear + 5; year++) {
            LocalDate target = LocalDate.of(year, 12, 31);
            BigDecimal fair = priceFromRegression(stats.intercept, stats.slope, stats.originDate, target);
            BigDecimal conservative = scaled(fair.multiply(stats.q25Ratio));
            BigDecimal base = scaled(fair.multiply(stats.q50Ratio));
            BigDecimal bullish = scaled(fair.multiply(stats.q75Ratio));
            out.add(new ForecastPoint(year, conservative, base, bullish, scaled(fair), commentaryForYear(year, conservative, base, bullish)));
        }
        return out;
    }

    private List<HistoricalPricePoint> buildYearlyHistory(List<DailyPoint> history) {
        Map<Integer, DailyPoint> byYear = new LinkedHashMap<>();
        for (DailyPoint point : history) {
            byYear.put(point.date.getYear(), point);
        }
        List<HistoricalPricePoint> out = new ArrayList<>();
        List<Integer> years = new ArrayList<>(byYear.keySet());
        Collections.sort(years);
        int from = Math.max(0, years.size() - 8);
        for (int i = from; i < years.size(); i++) {
            Integer year = years.get(i);
            DailyPoint point = byYear.get(year);
            if (point != null) {
                out.add(new HistoricalPricePoint(YEAR_FORMAT.format(point.date), scaled(point.price)));
            }
        }
        return out;
    }

    private List<String> buildForecastNotes(Stats stats) {
        List<String> notes = new ArrayList<>();
        notes.add("预测并非喊单：使用 BTC 全历史日线做对数回归，反映长期趋势，不代表短期一定按该路径运行。");
        notes.add("区间上下沿来自历史偏离分位数，分别取 25%、50%、75% 分位，避免只给单点价格造成误导。");
        notes.add("当前 BTC 位于长期公允值的 " + scaled(percent(stats.currentPrice.divide(stats.currentFairValue, 8, RoundingMode.HALF_UP))).toPlainString() + "，说明价格仍然更受周期波动影响。");
        notes.add("如果 Fear & Greed、999 指数、Pi Cycle 同时转热，实际价格通常会偏向预测区间上沿甚至短暂超冲。反之亦然。");
        return notes;
    }

    private String commentaryForYear(int year, BigDecimal conservative, BigDecimal base, BigDecimal bullish) {
        if (year == 2028) {
            return "下一轮减半年，波动通常放大，适合重点观察区间上沿突破情况。";
        }
        if (year > 2028) {
            return "进入下一轮周期定价阶段，更看长期 adoption 与宏观流动性。";
        }
        if (bullish.compareTo(new BigDecimal("100000")) >= 0) {
            return "若周期继续扩张，该年度有望重新测试或突破 10 万美元关口。";
        }
        return "更像估值修复年，重点看是否站稳长期公允值上方。";
    }

    private BigDecimal calculateDipIndex(Stats stats, FearGreed fearGreed) {
        BigDecimal drawdownScore = clamp(percent(BigDecimal.ONE.subtract(stats.currentPrice.divide(stats.athPrice, 8, RoundingMode.HALF_UP))), ZERO, new BigDecimal("100"));
        BigDecimal undervaluation = ZERO;
        if (stats.currentPrice.compareTo(stats.ma730) < 0) {
            undervaluation = clamp(percent(BigDecimal.ONE.subtract(stats.currentPrice.divide(stats.ma730, 8, RoundingMode.HALF_UP))), ZERO, new BigDecimal("100"));
        }
        BigDecimal emotion = new BigDecimal("100").subtract(fearGreed.value);
        return drawdownScore.multiply(new BigDecimal("0.45"))
                .add(undervaluation.multiply(new BigDecimal("0.35")))
                .add(emotion.multiply(new BigDecimal("0.20")));
    }

    private List<DailyPoint> fetchDailyHistory() throws IOException {
        Map<LocalDate, DailyPoint> merged = new LinkedHashMap<>();
        long toTs = 0L;
        for (int page = 0; page < 5; page++) {
            String url = "https://min-api.cryptocompare.com/data/v2/histoday?fsym=BTC&tsym=USD&limit=2000";
            if (toTs > 0L) {
                url += "&toTs=" + toTs;
            }
            Request request = new Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "OpenWeb4/1.0")
                    .build();
            try (Response response = http.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("CryptoCompare HTTP " + response.code());
                }
                JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonObject data = root.getAsJsonObject("Data");
                JsonArray rows = data.getAsJsonArray("Data");
                if (rows == null || rows.size() == 0) {
                    break;
                }
                long firstTs = 0L;
                for (int i = 0; i < rows.size(); i++) {
                    JsonObject row = rows.get(i).getAsJsonObject();
                    long ts = row.get("time").getAsLong();
                    firstTs = firstTs == 0L ? ts : Math.min(firstTs, ts);
                    double close = row.has("close") && !row.get("close").isJsonNull() ? row.get("close").getAsDouble() : 0D;
                    if (close <= 0D) {
                        continue;
                    }
                    LocalDate date = Instant.ofEpochSecond(ts).atZone(UTC).toLocalDate();
                    merged.put(date, new DailyPoint(date, BigDecimal.valueOf(close)));
                }
                if (firstTs <= 1279324800L) {
                    break;
                }
                toTs = firstTs - 86400L;
            }
        }
        List<DailyPoint> out = new ArrayList<>(merged.values());
        out.sort(Comparator.comparing(point -> point.date));
        return out;
    }

    private void applyLiveSpotPrice(List<DailyPoint> history) {
        if (history.isEmpty()) {
            return;
        }
        CryptoPrice live = cryptoPriceService.getBitcoinPrice();
        if (live == null || live.getPrice() == null || live.getPrice().compareTo(ZERO) <= 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        DailyPoint last = history.get(history.size() - 1);
        if (today.isAfter(last.date)) {
            history.add(new DailyPoint(today, live.getPrice()));
        } else {
            last.price = live.getPrice();
        }
    }

    private FearGreed fetchFearGreed() {
        Request request = new Request.Builder()
                .url("https://api.alternative.me/fng/?limit=1&format=json")
                .header("Accept", "application/json")
                .header("User-Agent", "OpenWeb4/1.0")
                .build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Alternative.me HTTP " + response.code());
            }
            JsonObject root = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");
            if (data == null || data.size() == 0) {
                throw new IOException("Fear & Greed payload is empty");
            }
            JsonObject first = data.get(0).getAsJsonObject();
            BigDecimal value = new BigDecimal(first.get("value").getAsString());
            String label = first.get("value_classification").getAsString();
            return new FearGreed(value, label);
        } catch (Exception e) {
            log.warn("Failed to fetch fear & greed index: {}", e.getMessage());
            return new FearGreed(new BigDecimal("50"), "Neutral");
        }
    }

    private Stats calculateStats(List<DailyPoint> history) {
        int size = history.size();
        double[] closes = new double[size];
        double[] prefix = new double[size + 1];

        for (int i = 0; i < size; i++) {
            closes[i] = history.get(i).price.doubleValue();
            prefix[i + 1] = prefix[i] + closes[i];
        }

        LocalDate originDate = history.get(0).date;
        Regression regression = regression(history, originDate);
        BigDecimal currentPrice = history.get(size - 1).price;
        BigDecimal athPrice = maxPrice(history);
        BigDecimal ma111 = movingAverage(prefix, size - 1, 111);
        BigDecimal ma200 = movingAverage(prefix, size - 1, 200);
        BigDecimal ma350 = movingAverage(prefix, size - 1, 350);
        BigDecimal ma730 = movingAverage(prefix, size - 1, 730);
        BigDecimal currentFairValue = priceFromRegression(regression.intercept, regression.slope, originDate, history.get(size - 1).date);

        List<BigDecimal> deviationRatios = new ArrayList<>();
        for (DailyPoint point : history) {
            if (point.date.getYear() < 2014) {
                continue;
            }
            BigDecimal fair = priceFromRegression(regression.intercept, regression.slope, originDate, point.date);
            if (fair.compareTo(ZERO) <= 0) {
                continue;
            }
            deviationRatios.add(point.price.divide(fair, 8, RoundingMode.HALF_UP));
        }
        deviationRatios.sort(Comparator.naturalOrder());

        Stats stats = new Stats();
        stats.originDate = originDate;
        stats.intercept = regression.intercept;
        stats.slope = regression.slope;
        stats.currentPrice = currentPrice;
        stats.athPrice = athPrice;
        stats.ma111 = ma111;
        stats.ma200 = ma200;
        stats.ma350 = ma350;
        stats.ma730 = ma730;
        stats.currentFairValue = currentFairValue;
        stats.q25Ratio = percentile(deviationRatios, 0.25D);
        stats.q50Ratio = percentile(deviationRatios, 0.50D);
        stats.q75Ratio = percentile(deviationRatios, 0.75D);
        return stats;
    }

    private Regression regression(List<DailyPoint> history, LocalDate originDate) {
        int n = 0;
        double sumX = 0D;
        double sumY = 0D;
        double sumXY = 0D;
        double sumX2 = 0D;
        for (DailyPoint point : history) {
            if (point.price.compareTo(ZERO) <= 0) {
                continue;
            }
            double day = Math.max(1D, point.date.toEpochDay() - originDate.toEpochDay() + 1D);
            double x = Math.log(day);
            double y = Math.log(point.price.doubleValue());
            n++;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        return new Regression(intercept, slope);
    }

    private BigDecimal movingAverage(double[] prefix, int endIndex, int period) {
        if (endIndex + 1 < period) {
            return ZERO;
        }
        double sum = prefix[endIndex + 1] - prefix[endIndex + 1 - period];
        return scaled(BigDecimal.valueOf(sum / period));
    }

    private BigDecimal percentile(List<BigDecimal> values, double q) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ONE;
        }
        double index = (values.size() - 1) * q;
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return values.get(lower);
        }
        BigDecimal lowerValue = values.get(lower);
        BigDecimal upperValue = values.get(upper);
        BigDecimal fraction = BigDecimal.valueOf(index - lower);
        return lowerValue.multiply(BigDecimal.ONE.subtract(fraction)).add(upperValue.multiply(fraction));
    }

    private BigDecimal priceFromRegression(double intercept, double slope, LocalDate originDate, LocalDate targetDate) {
        double day = Math.max(1D, targetDate.toEpochDay() - originDate.toEpochDay() + 1D);
        return scaled(BigDecimal.valueOf(Math.exp(intercept + slope * Math.log(day))));
    }

    private BigDecimal maxPrice(List<DailyPoint> history) {
        BigDecimal max = ZERO;
        for (DailyPoint point : history) {
            if (point.price.compareTo(max) > 0) {
                max = point.price;
            }
        }
        return scaled(max);
    }

    private BtcMarketSnapshot fallbackSnapshot() {
        BtcMarketSnapshot snapshot = new BtcMarketSnapshot();
        CryptoPrice price = cryptoPriceService.getBitcoinPrice();
        BigDecimal current = price != null && price.getPrice() != null ? price.getPrice() : ZERO;
        snapshot.setCurrentPrice(current);
        snapshot.setAthPrice(current);
        snapshot.setMa200(current);
        snapshot.setTwoYearMa(current);
        snapshot.setPowerLawFairValue(current);
        snapshot.setCurrentVsPowerLaw(new BigDecimal("100"));
        snapshot.setCurrentVs200Ma(new BigDecimal("100"));
        snapshot.setDrawdownFromAth(ZERO);
        snapshot.setFearGreedValue(new BigDecimal("50"));
        snapshot.setFearGreedLabel("Neutral");
        snapshot.setForecastMethod("数据源不可用时的降级快照");
        snapshot.setDataSource("CoinGecko 当前价格（降级）");
        snapshot.setUpdatedAt(LocalDateTime.now());
        snapshot.setForecastNotes(List.of("当前未能成功拉取完整历史数据，页面展示为降级快照。"));
        return snapshot;
    }

    private String signalFor999(BigDecimal value) {
        if (value.compareTo(new BigDecimal("70")) < 0) {
            return "价值区";
        }
        if (value.compareTo(new BigDecimal("120")) <= 0) {
            return "中性定投区";
        }
        return "过热区";
    }

    private String toneFor999(BigDecimal value) {
        if (value.compareTo(new BigDecimal("70")) < 0) {
            return "good";
        }
        if (value.compareTo(new BigDecimal("120")) <= 0) {
            return "neutral";
        }
        return "danger";
    }

    private String signalForAhr999(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.45")) < 0) {
            return "抄底区";
        }
        if (value.compareTo(new BigDecimal("1.20")) <= 0) {
            return "定投区";
        }
        return "偏热区";
    }

    private String toneForAhr999(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.45")) < 0) {
            return "good";
        }
        if (value.compareTo(new BigDecimal("1.20")) <= 0) {
            return "neutral";
        }
        return "danger";
    }

    private String signalForDip(BigDecimal value) {
        if (value.compareTo(new BigDecimal("65")) >= 0) {
            return "可分批关注";
        }
        if (value.compareTo(new BigDecimal("40")) >= 0) {
            return "中性观察";
        }
        return "追高风险";
    }

    private String toneForDip(BigDecimal value) {
        if (value.compareTo(new BigDecimal("65")) >= 0) {
            return "good";
        }
        if (value.compareTo(new BigDecimal("40")) >= 0) {
            return "neutral";
        }
        return "danger";
    }

    private String signalForTwoYearMultiplier(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.50")) < 0) {
            return "低估区";
        }
        if (value.compareTo(BigDecimal.ONE) <= 0) {
            return "正常区";
        }
        return "高热区";
    }

    private String toneForTwoYearMultiplier(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.50")) < 0) {
            return "good";
        }
        if (value.compareTo(BigDecimal.ONE) <= 0) {
            return "neutral";
        }
        return "danger";
    }

    private String signalForPiCycle(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.80")) < 0) {
            return "安全区";
        }
        if (value.compareTo(BigDecimal.ONE) <= 0) {
            return "升温区";
        }
        return "顶部警戒";
    }

    private String toneForPiCycle(BigDecimal value) {
        if (value.compareTo(new BigDecimal("0.80")) < 0) {
            return "good";
        }
        if (value.compareTo(BigDecimal.ONE) <= 0) {
            return "warning";
        }
        return "danger";
    }

    private String toneByFearGreed(BigDecimal value) {
        if (value.compareTo(new BigDecimal("25")) <= 0) {
            return "good";
        }
        if (value.compareTo(new BigDecimal("55")) < 0) {
            return "neutral";
        }
        if (value.compareTo(new BigDecimal("75")) < 0) {
            return "warning";
        }
        return "danger";
    }

    private BigDecimal percent(BigDecimal ratio) {
        return ratio.multiply(new BigDecimal("100"));
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    private BigDecimal scaled(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static class DailyPoint {
        private final LocalDate date;
        private BigDecimal price;

        private DailyPoint(LocalDate date, BigDecimal price) {
            this.date = date;
            this.price = price;
        }
    }

    private static class Regression {
        private final double intercept;
        private final double slope;

        private Regression(double intercept, double slope) {
            this.intercept = intercept;
            this.slope = slope;
        }
    }

    private static class FearGreed {
        private final BigDecimal value;
        private final String label;

        private FearGreed(BigDecimal value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    private static class Stats {
        private LocalDate originDate;
        private double intercept;
        private double slope;
        private BigDecimal currentPrice;
        private BigDecimal athPrice;
        private BigDecimal ma111;
        private BigDecimal ma200;
        private BigDecimal ma350;
        private BigDecimal ma730;
        private BigDecimal currentFairValue;
        private BigDecimal q25Ratio;
        private BigDecimal q50Ratio;
        private BigDecimal q75Ratio;
    }
}
