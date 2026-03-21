package com.openweb4.scheduler;

import com.openweb4.service.CryptoPriceService;
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

    public DataFetchScheduler(CryptoPriceService cryptoPriceService,
                              WhaleTrackingService whaleTrackingService,
                              NewsService newsService,
                              TweetService tweetService) {
        this.cryptoPriceService = cryptoPriceService;
        this.whaleTrackingService = whaleTrackingService;
        this.newsService = newsService;
        this.tweetService = tweetService;
    }

    @Scheduled(fixedRate = 60000)
    public void fetchCryptoPrices() {
        log.debug("Fetching crypto prices");
        try {
            cryptoPriceService.getBitcoinPrice();
            cryptoPriceService.getEthereumPrice();
            cryptoPriceService.getTetherPrice();
        } catch (Exception e) {
            log.warn("Failed to fetch crypto prices", e);
        }
    }

    @Scheduled(fixedRate = 300000)
    public void fetchWhaleTransactions() {
        log.debug("Fetching whale transactions");
        try {
            whaleTrackingService.getRecentWhaleTransactions();
        } catch (Exception e) {
            log.warn("Failed to fetch whale transactions", e);
        }
    }

    @Scheduled(fixedRate = 1800000)
    public void fetchNews() {
        log.debug("Fetching crypto news");
        try {
            newsService.fetchCryptoNews();
        } catch (Exception e) {
            log.warn("Failed to fetch news", e);
        }
    }


    @Scheduled(fixedRate = 1800000)
    public void fetchLatestTweets() {
        log.debug("Fetching latest KOL tweets");
        try {
            tweetService.fetchLatestTweets();
        } catch (Exception e) {
            log.warn("Failed to fetch latest tweets", e);
        }
    }
}
