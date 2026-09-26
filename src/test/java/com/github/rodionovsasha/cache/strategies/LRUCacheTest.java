package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LRUCacheTest {
    @Test
    void shouldEvictLeastRecentlyUsedEntryFromFirstLevel() {
        try (var cache = new TwoLevelCache<Integer, String>(2, 0, StrategyType.LRU)) {
            cache.put(1, "one");
            cache.put(2, "two");
            cache.get(1);
            cache.put(3, "three");

            assertTrue(cache.isObjectPresent(1));
            assertFalse(cache.isObjectPresent(2));
            assertTrue(cache.isObjectPresent(3));
        }
    }
}
