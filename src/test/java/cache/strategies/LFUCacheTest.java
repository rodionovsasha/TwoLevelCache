package cache.strategies;

import cache.TwoLevelCache;
import org.junit.After;
import org.junit.Test;

import static cache.strategies.StrategyType.LFU;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Alexander
 */

public class LFUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldMoveObjectFromCacheTest() {
        twoLevelCache = new TwoLevelCache<>(2, 2, LFU);

        twoLevelCache.putObjectIntoCache(0, "String 0");
        twoLevelCache.getObjectFromCache(0);
        twoLevelCache.getObjectFromCache(0);
        twoLevelCache.putObjectIntoCache(1, "String 1");
        twoLevelCache.getObjectFromCache(1); // Least Frequently Used - will be removed
        twoLevelCache.putObjectIntoCache(2, "String 2");
        twoLevelCache.getObjectFromCache(2);
        twoLevelCache.getObjectFromCache(2);
        twoLevelCache.putObjectIntoCache(3, "String 3");
        twoLevelCache.getObjectFromCache(3);
        twoLevelCache.getObjectFromCache(3);

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertTrue(twoLevelCache.isObjectPresent(1));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.isObjectPresent(3));

        twoLevelCache.putObjectIntoCache(4, "String 4");
        twoLevelCache.getObjectFromCache(4);
        twoLevelCache.getObjectFromCache(4);

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertFalse(twoLevelCache.isObjectPresent(1)); // Least Frequently Used - has been removed
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.isObjectPresent(3));
        assertTrue(twoLevelCache.isObjectPresent(4));
    }
}