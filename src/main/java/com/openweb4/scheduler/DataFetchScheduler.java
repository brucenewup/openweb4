package com.openweb4.scheduler;

import com.openweb4.service.ContractDataService;
import com.openweb4.service.CryptoPriceService;
import com.openweb4.service.MarketBriefingService;
import com.openweb4.service.NewsService;
import com.openweb4.service.WhaleTrackingService;
import com.openweb4.service.TweetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataFetchScheduler.class);

    private final CryptoPriceService cryptoPriceService;
    private final WhaleTrackingService whaleTrackingService;
    private final NewsService newsService;
    private final TweetService tweetService;
    private final ContractDataService contractDataService;
    private final MarketBriefingService marketBriefingService;

    public DataFetchScheduler(CryptoPriceService cryptoPriceService,
                              WhaleTrackingService whaleTrackingService,
                              NewsService newsService,
                              TweetService tweetService,
                              ContractDataService contractDataService,
                              MarketBriefingService marketBriefingService) {
        this.cryptoPriceService = cryptoPriceService;
        this.whaleTrackingService = whaleTrackingService;
        this.newsService = newsService;
        this.tweetService = tweetService;
        this.contractDataService = contractDataService;
        this.marketBriefingService = marketBriefingService;
    }

    @Scheduled(fixedRate = 60000)
    public void fetchContractData() {
        log.debug("Fetching contract data (BTC/ETH)");
        executeWithRetry(() -> {
            contractDataService.getContractData("BTCUSDT");
            contractDataService.getContractData("ETHUSDT");
        }, "contract data");
    }

    @Scheduled(fixedRate = 60000)
    public void fetchCryptoPrices() {
        log.debug("Fetching crypto prices");
        executeWithRetry(() -> {
            cryptoPriceService.getBitcoinPrice();
            cryptoPriceService.getEthereumPrice();
            cryptoPriceService.getTetherPrice();
        }, "crypto prices");
    }

    @Scheduled(fixedRate = 300000)
    public void fetchWhaleTransactions() {
        log.debug("Fetching whale transactions");
        executeWithRetry(() -> {
            whaleTrackingService.getRecentWhaleTransactions();
        }, "whale transactions");
    }

    @Scheduled(fixedRate = 1800000)
    public void fetchNews() {
        log.debug("Fetching crypto news");
        executeWithRetry(() -> {
            newsService.fetchCryptoNews();
        }, "crypto news");
    }


    @Scheduled(fixedRate = 1800000)
    public void fetchLatestTweets() {
        log.debug("Fetching latest KOL tweets");
        executeWithRetry(() -> {
            tweetService.fetchLatestTweets();
        }, "latest tweets");
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyMarketBriefing() {
        log.info("Generating daily market briefing at 8:00 AM");
        executeWithRetry(() -> {
            marketBriefingService.generateBriefing();
        }, "daily market briefing");
    }

    /**
     * 带重试机制的任务执行器
     * @param task 要执行的任务
     * @param taskName 任务名称（用于日志）
     */
    private void executeWithRetry(Runnable task, String taskName) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                task.run();
                return; // 成功则退出
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn("Failed to fetch {} (attempt {}/{}): {}", taskName, attempt, maxRetries, e.getMessage());
                    try {
                        Thread.sleep(1000 * attempt); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted for {}", taskName);
                        return;
                    }
                } else {
                    log.error("All {} retries exhausted for {}", maxRetries, taskName, e);
                    // TODO: 发送告警（邮件/钉钉/Slack）
                }
            }
        }
    }
}
