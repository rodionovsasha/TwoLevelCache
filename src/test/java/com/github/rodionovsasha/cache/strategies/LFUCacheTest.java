package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LFUCacheTest {
    @Test
    void shouldEvictLeastFrequentlyUsedEntryFromFirstLevel() {
        try (var cache = new TwoLevelCache<Integer, String>(2, 0, StrategyType.LFU)) {
            cache.put(1, "one");
            cache.get(1);
            cache.get(1);
            cache.put(2, "two");
            cache.put(3, "three");

            assertTrue(cache.isObjectPresent(1));
            assertFalse(cache.isObjectPresent(2));
            assertTrue(cache.isObjectPresent(3));
        }
    }
}
