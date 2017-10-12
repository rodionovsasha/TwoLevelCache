package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.After;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.github.rodionovsasha.cache.strategies.StrategyType.MRU;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Alexander
 */

public class MRUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldMoveObjectFromCacheTest() {
        twoLevelCache = new TwoLevelCache<>(2, 2, MRU);

        // i=3 - Most Recently Used - will be removed
        IntStream.range(0, 4).forEach(i -> {
            twoLevelCache.putToCache(i, "String " + i);
            assertTrue(twoLevelCache.isObjectPresent(i));
            twoLevelCache.getFromCache(i);
        });

        twoLevelCache.putToCache(4, "String 4");

        assertTrue(twoLevelCache.isObjectPresent(0));
        assertTrue(twoLevelCache.isObjectPresent(1));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertFalse(twoLevelCache.isObjectPresent(3)); //Most Recently Used - has been removed
        assertTrue(twoLevelCache.isObjectPresent(4));
    }
}
