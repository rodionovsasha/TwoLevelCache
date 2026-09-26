package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.LRUStrategy;
import com.github.rodionovsasha.cache.strategies.MRUStrategy;
import com.github.rodionovsasha.cache.strategies.StrategyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwoLevelCacheTest {
    @Test
    void shouldWriteThroughToBothLevels() {
        try (var cache = new TwoLevelCache<Integer, String>(1, 2)) {
            assertTrue(cache.put(1, "one"));

            assertEquals("one", cache.get(1));
            assertTrue(cache.getFirstLevelCache().isObjectPresent(1));
            assertTrue(cache.getSecondLevelCache().isObjectPresent(1));
        }
    }

    @Test
    void shouldPromoteSecondLevelHitAndKeepBackingCopy() {
        try (var cache = new TwoLevelCache<Integer, String>(1, 2, StrategyType.LRU)) {
            cache.put(1, "one");
            cache.put(2, "two");

            assertFalse(cache.getFirstLevelCache().isObjectPresent(1));
            assertTrue(cache.getSecondLevelCache().isObjectPresent(1));
            assertEquals("one", cache.get(1));
            assertTrue(cache.getFirstLevelCache().isObjectPresent(1));
            assertTrue(cache.getSecondLevelCache().isObjectPresent(1));
        }
    }

    @Test
    void shouldConfigureStrategiesIndependently() {
        try (var cache = new TwoLevelCache<Integer, String>(2, 3, StrategyType.LRU, StrategyType.MRU)) {
            assertInstanceOf(LRUStrategy.class, cache.getFirstLevelStrategy());
            assertInstanceOf(MRUStrategy.class, cache.getSecondLevelStrategy());
        }
    }

    @Test
    void shouldNotCreateStaleMetadataWhenSerializationFails() {
        try (var cache = new TwoLevelCache<Integer, Serializable>(0, 1)) {
            assertFalse(cache.put(1, new BrokenSerializable()));
            assertFalse(cache.getSecondLevelStrategy().isObjectPresent(1));

            assertTrue(cache.put(2, "two"));
            assertTrue(cache.put(3, "three"));
            assertFalse(cache.isObjectPresent(2));
            assertEquals("three", cache.get(3));
        }
    }

    @Test
    void shouldRemoveFromBothLevels() {
        try (var cache = new TwoLevelCache<Integer, String>(1, 1)) {
            cache.put(1, "one");
            cache.remove(1);

            assertNull(cache.get(1));
            assertFalse(cache.getFirstLevelCache().isObjectPresent(1));
            assertFalse(cache.getSecondLevelCache().isObjectPresent(1));
        }
    }

    @Test
    void shouldAcceptNonSerializableKeys() {
        try (var cache = new TwoLevelCache<NonSerializableKey, String>(1, 1)) {
            var key = new NonSerializableKey("key");
            cache.put(key, "value");
            assertEquals("value", cache.get(key));
        }
    }

    @Test
    void shouldValidateConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new TwoLevelCacheConfig(-1, 1, StrategyType.LFU, StrategyType.LFU));
        assertThrows(IllegalArgumentException.class,
                () -> new TwoLevelCacheConfig(1, -1, StrategyType.LFU, StrategyType.LFU));
        assertThrows(IllegalArgumentException.class,
                () -> new TwoLevelCacheConfig(0, 0, StrategyType.LFU, StrategyType.LFU));
        assertThrows(NullPointerException.class,
                () -> new TwoLevelCacheConfig(1, 1, null, StrategyType.LFU));
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldSupportLegacyOperationsAndReportLogicalSize() {
        try (var cache = new TwoLevelCache<Integer, String>(1, 1)) {
            assertTrue(cache.hasEmptyPlace());
            cache.putToCache(1, "one");
            assertFalse(cache.hasEmptyPlace());
            assertEquals("one", cache.getFromCache(1));
            assertEquals(1, cache.getCacheSize());
            assertTrue(cache.isObjectPresent(1));
            assertTrue(cache.getStrategy().isObjectPresent(1));
            cache.removeFromCache(1);
            assertFalse(cache.isObjectPresent(1));
            cache.clearCache();
            assertEquals(0, cache.getCacheSize());
        }
    }

    @Test
    void shouldUpdateEntriesAndSupportEachSingleLevel() {
        try (var memoryOnly = new TwoLevelCache<Integer, String>(1, 0)) {
            assertTrue(memoryOnly.put(1, "one"));
            assertTrue(memoryOnly.put(1, "updated"));
            assertEquals("updated", memoryOnly.get(1));
        }
        try (var diskOnly = new TwoLevelCache<Integer, String>(0, 1)) {
            assertTrue(diskOnly.put(1, "one"));
            assertTrue(diskOnly.put(1, "updated"));
            assertEquals("updated", diskOnly.get(1));
        }
    }

    @Test
    void shouldDiscardStaleStrategyEntriesBeforeEviction() {
        try (var memoryOnly = new TwoLevelCache<Integer, String>(1, 0)) {
            memoryOnly.getFirstLevelStrategy().onInsert(-1);
            memoryOnly.put(1, "one");
            memoryOnly.put(2, "two");
            assertEquals("two", memoryOnly.get(2));
        }
        try (var diskOnly = new TwoLevelCache<Integer, String>(0, 1)) {
            diskOnly.getSecondLevelStrategy().onInsert(-1);
            diskOnly.put(1, "one");
            diskOnly.put(2, "two");
            assertEquals("two", diskOnly.get(2));
        }
    }

    @Test
    void shouldForgetCorruptedSecondLevelEntry() throws Exception {
        try (var cache = new TwoLevelCache<Integer, String>(0, 1)) {
            cache.put(1, "one");
            Path file;
            try (var files = Files.list(cache.getSecondLevelCache().getStorageDirectory())) {
                file = files.findFirst().orElseThrow();
            }
            Files.writeString(file, "broken");
            assertNull(cache.get(1));
            assertFalse(cache.isObjectPresent(1));
        }
    }

    @Test
    void shouldCreateStorageUnderConfiguredRoot(@TempDir Path root) throws Exception {
        var config = new TwoLevelCacheConfig(0, 1, StrategyType.LFU, StrategyType.LFU, root);
        try (var cache = new TwoLevelCache<Integer, String>(config)) {
            cache.put(1, "one");
            assertEquals(root, cache.getSecondLevelCache().getStorageDirectory().getParent());
        }
        try (var files = Files.list(root)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void shouldRemoveStaleDiskCopyAfterFailedUpdate() {
        try (var cache = new TwoLevelCache<Integer, Serializable>(1, 1)) {
            cache.put(1, "original");
            assertTrue(cache.put(1, new BrokenSerializable()));
            assertFalse(cache.getSecondLevelCache().isObjectPresent(1));
            assertFalse(cache.getSecondLevelStrategy().isObjectPresent(1));

            try (var diskOnly = new TwoLevelCache<Integer, Serializable>(0, 1)) {
                diskOnly.putToCache(1, new BrokenSerializable());
                assertFalse(diskOnly.isObjectPresent(1));
                diskOnly.put(2, "value");
                assertFalse(diskOnly.put(3, new BrokenSerializable()));
                assertEquals("value", diskOnly.get(2));
            }
        }
    }

    @Test
    void shouldEvaluateBothLevelsForPresenceAndAvailableSpace() {
        try (var cache = new TwoLevelCache<Integer, String>(1, 2)) {
            cache.put(1, "one");
            cache.getSecondLevelCache().removeFromCache(1);
            assertTrue(cache.hasEmptyPlace());
            assertTrue(cache.isObjectPresent(1));
        }

        try (var cache = new TwoLevelCache<Integer, String>(1, 2)) {
            cache.put(1, "one");
            cache.put(2, "two");
            assertTrue(cache.isObjectPresent(1));
            assertFalse(cache.isObjectPresent(99));
            assertFalse(cache.hasEmptyPlace());
        }
    }

    private static final class BrokenSerializable implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @Serial
        private void writeObject(ObjectOutputStream stream) throws IOException {
            throw new NotSerializableException("Intentional test failure");
        }
    }

    private static final class NonSerializableKey {
        private final String value;

        private NonSerializableKey(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof NonSerializableKey key)) {
                return false;
            }
            return Objects.equals(value, key.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
