package cache;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public class MRUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldMoveObjectFromCacheTest() {
        twoLevelCache = new TwoLevelCache<>(2, 2, CacheApp.StrategyType.MRU);

        // i=3 - Most Recently Used - will be removed
        for (int i = 0; i < 4; i++) {
            twoLevelCache.putObjectIntoCache(i, "String " + i);
            assertTrue(twoLevelCache.isObjectPresent(i));
            twoLevelCache.getObjectFromCache(i);
        }

        twoLevelCache.putObjectIntoCache(4, "String 4");

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertTrue(twoLevelCache.isObjectPresent(1));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertFalse(twoLevelCache.isObjectPresent(3));
        assertTrue(twoLevelCache.isObjectPresent(4));
    }
}
