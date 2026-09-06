package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.StrategyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

public class TwoLevelCacheTest {
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";

    private TwoLevelCache<Integer, String> twoLevelCache;

    @BeforeEach
    public void init() {
        twoLevelCache = new TwoLevelCache<>(1, 1);
    }

    @AfterEach
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    void shouldReportSpaceUntilBothLevelsAreFull() {
        assertTrue(twoLevelCache.hasEmptyPlace());
        twoLevelCache.putToCache(0, VALUE1);
        assertTrue(twoLevelCache.hasEmptyPlace());
        twoLevelCache.putToCache(1, VALUE2);
        assertFalse(twoLevelCache.hasEmptyPlace());
    }

    @Test
    void shouldDiscardStaleVictimMetadataWithoutRemovingStoredEntries() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);
        // Simulate metadata left behind by a failed storage write.
        twoLevelCache.getStrategy().onInsert(-1);
        twoLevelCache.getFromCache(0);
        twoLevelCache.getFromCache(1);
        twoLevelCache.putToCache(2, VALUE3);
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(-1));
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getFromCache(1));
        assertEquals(2, twoLevelCache.getCacheSize());
        // Documents current behavior: this insertion is lost when the victim is stale.
        assertNull(twoLevelCache.getFromCache(2));
    }

    @Test
    public void shouldPutGetAndRemoveObjectTest() {
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.removeFromCache(0);
        assertNull(twoLevelCache.getFromCache(0));
    }

    @Test
    public void shouldRemoveObjectFromFirstLevelTest() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(1));

        twoLevelCache.removeFromCache(0);

        assertNull(twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(1));
    }

    @Test
    public void shouldRemoveObjectFromSecondLevelTest() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(1));

        twoLevelCache.removeFromCache(1);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertNull(twoLevelCache.getSecondLevelCache().getFromCache(1));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertNull(twoLevelCache.getFromCache(111));
    }

    @Test
    public void shouldRemoveDuplicatedObjectFromSecondLevelWhenFirstLevelHasEmptyPlaceTest() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasEmptyPlace());

        twoLevelCache.getSecondLevelCache().putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getFromCache(0));

        twoLevelCache.putToCache(0, VALUE1);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenFirstLevelHasEmptyPlaceTest() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectExistsInFirstLevelCacheTest() {
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());

        // put the same key with other value
        twoLevelCache.putToCache(0, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());
    }

    @Test
    public void shouldPutObjectIntoCacheWhenSecondLevelHasEmptyPlaceTest() {
        IntStream.range(0, 1).forEach(i -> twoLevelCache.putToCache(i, "String " + i));

        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        assertTrue(twoLevelCache.getSecondLevelCache().hasEmptyPlace());

        twoLevelCache.putToCache(2, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getFromCache(2));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(2));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectExistsInSecondLevelTest() {
        IntStream.range(0, 1).forEach(i -> twoLevelCache.putToCache(i, "String " + i));

        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());

        twoLevelCache.putToCache(2, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getFromCache(2));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getFromCache(2));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());

        // put the same key with other value
        twoLevelCache.putToCache(2, VALUE3);

        assertEquals(VALUE3, twoLevelCache.getFromCache(2));
        assertEquals(VALUE3, twoLevelCache.getSecondLevelCache().getFromCache(2));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectShouldBeReplacedTest() {
        IntStream.range(0, 2).forEach(i -> twoLevelCache.putToCache(i, "String " + i));

        assertFalse(twoLevelCache.hasEmptyPlace());
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(3));

        twoLevelCache.putToCache(3, VALUE3);

        assertEquals(VALUE3, twoLevelCache.getFromCache(3));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(3));
        assertTrue(twoLevelCache.getFirstLevelCache().isObjectPresent(3));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(3));
    }

    @Test
    public void shouldRemoveReplacedObjectFromStrategyTest() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);

        twoLevelCache.putToCache(2, VALUE3);

        assertFalse(twoLevelCache.isObjectPresent(0));
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(0));
        assertTrue(twoLevelCache.isObjectPresent(2));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(2));
    }

    @Test
    public void shouldContinueCachingAfterRepeatedReplacementsTest() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);

        twoLevelCache.putToCache(2, VALUE3);
        twoLevelCache.putToCache(3, "value4");

        assertEquals("value4", twoLevelCache.getFromCache(3));
        assertEquals(2, twoLevelCache.getCacheSize());
    }

    @Test
    public void shouldGetCacheSizeTest() {
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.putToCache(1, VALUE2);
        assertEquals(2, twoLevelCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));

        twoLevelCache.putToCache(0, VALUE1);
        assertTrue(twoLevelCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));
        twoLevelCache.putToCache(0, VALUE1);
        assertTrue(twoLevelCache.hasEmptyPlace());

        twoLevelCache.putToCache(1, VALUE2);
        assertFalse(twoLevelCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        twoLevelCache.putToCache(0, VALUE1);
        twoLevelCache.putToCache(1, VALUE2);

        assertEquals(2, twoLevelCache.getCacheSize());
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(0));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(1));

        twoLevelCache.clearCache();

        assertEquals(0, twoLevelCache.getCacheSize());
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(0));
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(1));
    }

    @Test
    public void shouldUseLRUStrategyTest() {
        twoLevelCache = new TwoLevelCache<>(1, 1, StrategyType.LRU);
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldUseMRUStrategyTest() {
        twoLevelCache = new TwoLevelCache<>(1, 1, StrategyType.MRU);
        twoLevelCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldRejectInvalidCapacityTest() {
        assertThrows(IllegalArgumentException.class, () -> new TwoLevelCache<>(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new TwoLevelCache<>(1, -1));
        assertThrows(IllegalArgumentException.class, () -> new TwoLevelCache<>(0, 0));
    }

    @Test
    public void shouldSupportSerializableKeysWithoutNaturalOrderingTest() {
        TwoLevelCache<CustomKey, String> cache = new TwoLevelCache<>(1, 1);
        CustomKey key1 = new CustomKey("key1");
        CustomKey key2 = new CustomKey("key2");

        try {
            cache.putToCache(key1, VALUE1);
            cache.putToCache(key2, VALUE2);

            assertEquals(VALUE1, cache.getFromCache(key1));
            assertEquals(VALUE2, cache.getFromCache(key2));
        } finally {
            cache.clearCache();
        }
    }

    private static final class CustomKey implements Serializable {
        @Serial
        private static final long serialVersionUID = 1;

        private final String value;

        private CustomKey(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CustomKey customKey)) {
                return false;
            }
            return Objects.equals(value, customKey.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
