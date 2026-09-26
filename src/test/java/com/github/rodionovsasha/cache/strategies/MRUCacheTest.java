package com.github.rodionovsasha.cache.strategies;

import com.github.rodionovsasha.cache.TwoLevelCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MRUCacheTest {
    @Test
    void shouldEvictMostRecentlyUsedEntryFromFirstLevel() {
        try (var cache = new TwoLevelCache<Integer, String>(2, 0, StrategyType.MRU)) {
            cache.put(1, "one");
            cache.put(2, "two");
            cache.get(1);
            cache.put(3, "three");

            assertFalse(cache.isObjectPresent(1));
            assertTrue(cache.isObjectPresent(2));
            assertTrue(cache.isObjectPresent(3));
        }
    }
}
