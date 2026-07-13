package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.github.rodionovsasha.cache.strategies.StrategyType.LFU;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Aleksandr
 */

public class LFUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @AfterEach
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldMoveObjectFromCacheTest() {
        twoLevelCache = new TwoLevelCache<>(2, 2, LFU);

        twoLevelCache.putToCache(0, "String 0");
        twoLevelCache.getFromCache(0);
        twoLevelCache.getFromCache(0);
        twoLevelCache.putToCache(1, "String 1");
        twoLevelCache.getFromCache(1); // Least Frequently Used - will be removed
        twoLevelCache.putToCache(2, "String 2");
        twoLevelCache.getFromCache(2);
        twoLevelCache.getFromCache(2);
        twoLevelCache.putToCache(3, "String 3");
        twoLevelCache.getFromCache(3);
        twoLevelCache.getFromCache(3);

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertTrue(twoLevelCache.isObjectPresent(1));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.isObjectPresent(3));

        twoLevelCache.putToCache(4, "String 4");
        twoLevelCache.getFromCache(4);
        twoLevelCache.getFromCache(4);

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertFalse(twoLevelCache.isObjectPresent(1)); // Least Frequently Used - has been removed
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.isObjectPresent(3));
        assertTrue(twoLevelCache.isObjectPresent(4));
    }

    @Test
    public void shouldNotRemoveObjectIfNotPresentTest() {
        twoLevelCache = new TwoLevelCache<>(1, 1, LFU);

        twoLevelCache.putToCache(0, "String 0");
        twoLevelCache.putToCache(1, "String 1");

        twoLevelCache.removeFromCache(2);

    }
}
