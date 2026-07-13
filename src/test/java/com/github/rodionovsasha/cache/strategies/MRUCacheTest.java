package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.github.rodionovsasha.cache.strategies.StrategyType.MRU;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Aleksandr
 */

public class MRUCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @AfterEach
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
