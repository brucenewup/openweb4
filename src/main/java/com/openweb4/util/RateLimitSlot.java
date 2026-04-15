package com.openweb4.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用限流时间窗口槽位
 * 用于固定窗口限流算法
 */
public final class RateLimitSlot {
    private final long windowId;
    private final AtomicInteger count;

    public RateLimitSlot(long windowId) {
        this.windowId = windowId;
        this.count = new AtomicInteger(0);
    }

    public long getWindowId() {
        return windowId;
    }

    public AtomicInteger getCount() {
        return count;
    }
}
