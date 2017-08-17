package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.After;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.github.rodionovsasha.cache.strategies.StrategyType.LRU;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Alexander
 */

public class LRUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldMoveObjectFromCacheTest() {
        twoLevelCache = new TwoLevelCache<>(2, 2, LRU);

        // i=0 - Least Recently Used - will be removed
        IntStream.range(0, 4).forEach(i -> {
            twoLevelCache.putObjectIntoCache(i, "String " + i);
            assertTrue(twoLevelCache.isObjectPresent(i));
            twoLevelCache.getObjectFromCache(i);
        });

        twoLevelCache.putObjectIntoCache(4, "String 4");

        assertFalse(twoLevelCache.isObjectPresent(0)); //Least Recently Used - has been removed
        assertTrue(twoLevelCache.isObjectPresent(1));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.isObjectPresent(3));
        assertTrue(twoLevelCache.isObjectPresent(4));
    }
}
