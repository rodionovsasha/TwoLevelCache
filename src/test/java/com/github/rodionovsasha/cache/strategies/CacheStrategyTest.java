package com.github.rodionovsasha.cache.strategies;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheStrategyTest {
    @Test
    void shouldTrackRecencyAfterReadsAndUpdates() {
        for (CacheStrategy<String> strategy : List.of(new LRUStrategy<String>(), new MRUStrategy<String>())) {
            strategy.onInsert("a");
            strategy.onInsert("b");
            strategy.onAccess("a");
            boolean lru = strategy instanceof LRUStrategy;
            assertEquals(lru ? "b" : "a", strategy.getReplacedKey());
            strategy.onInsert("b");
            assertEquals(lru ? "a" : "b", strategy.getReplacedKey());
        }
    }

    @Test
    void shouldResolveEqualFrequenciesByInsertionOrderWithoutComparingKeys() {
        var strategy = new LFUStrategy<>();
        var first = new Object();
        var second = new Object();
        strategy.onInsert(first);
        strategy.onInsert(second);
        assertEquals(first, strategy.getReplacedKey());
        strategy.onAccess(first);
        assertEquals(second, strategy.getReplacedKey());
        strategy.onAccess(second);
        assertEquals(first, strategy.getReplacedKey());
    }

    @Test
    void shouldRemoveClearAndReuseEachStrategy() {
        for (CacheStrategy<String> strategy : List.of(
                new LFUStrategy<String>(), new LRUStrategy<String>(), new MRUStrategy<String>())) {
            assertTrue(strategy.selectVictim().isEmpty());
            assertThrows(NoSuchElementException.class, strategy::getReplacedKey);
            strategy.onAccess("missing");
            assertFalse(strategy.isObjectPresent("missing"));
            strategy.onInsert("a");
            strategy.onInsert("b");
            var victim = strategy.getReplacedKey();
            assertTrue(strategy.isObjectPresent(victim));
            strategy.removeObject(victim);
            assertFalse(strategy.isObjectPresent(victim));
            assertFalse(strategy.selectVictim().isEmpty());
            strategy.clear();
            assertTrue(strategy.selectVictim().isEmpty());
            strategy.onInsert("new");
            assertEquals("new", strategy.getReplacedKey());
        }
    }
}
