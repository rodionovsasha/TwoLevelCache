package com.github.rodionovsasha.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

class FileSystemCacheFailureTest {
    private FileSystemCache<Integer, Serializable> cache;
    private Path directory;

    @BeforeEach
    void setUp() {
        cache = new FileSystemCache<>(2);
        directory = cache.getStorageDirectory();
    }

    @AfterEach
    void tearDown() {
        cache.close();
    }

    @Test
    void shouldExposeDirectoryCreationFailureWithOriginalCause() {
        var failure = new IOException("Directory unavailable");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempDirectory("two-level-cache-")).thenThrow(failure);
            var exception = assertThrows(IllegalStateException.class, () -> {
                try (var ignored = new FileSystemCache<>(1)) {
                    // The constructor always throws in this test.
                }
            });
            assertSame(failure, exception.getCause());
        }
    }

    @Test
    void shouldNotRegisterEntryWhenCacheDirectoryIsMissing() throws IOException {
        Files.delete(directory);
        cache.putToCache(1, "value");
        assertEquals(0, cache.getCacheSize());
        assertFalse(cache.isObjectPresent(1));
    }

    @Test
    void shouldPreserveOldValueAndRemovePartialFileWhenSerializationFails() throws IOException {
        cache.putToCache(1, "original");
        cache.putToCache(1, new NonSerializableContent());
        assertEquals("original", cache.getFromCache(1));
        assertEquals(1, cache.getCacheSize());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void shouldReturnMissForCorruptedFile() throws IOException {
        cache.putToCache(1, "value");
        Files.writeString(storedFile(), "not a serialized object");
        assertNull(cache.getFromCache(1));
        assertFalse(cache.isObjectPresent(1));
    }

    @Test
    void shouldReturnMissWhenSerializedClassCannotBeLoaded() {
        cache.putToCache(1, new UnreadableContent());
        assertNull(cache.getFromCache(1));
        assertFalse(cache.isObjectPresent(1));
    }

    @Test
    void shouldRemoveIndexEntryEvenWhenBackingFileHasDisappeared() throws IOException {
        cache.putToCache(1, "value");
        Files.delete(storedFile());
        assertNull(cache.getFromCache(1));
        cache.removeFromCache(1);
        assertEquals(0, cache.getCacheSize());
        assertFalse(cache.isObjectPresent(1));
    }

    @Test
    void shouldClearIndexWhenDirectoryCannotBeWalked() throws IOException {
        cache.putToCache(1, "value");
        Files.delete(storedFile());
        Files.delete(directory);
        cache.clearCache();
        assertEquals(0, cache.getCacheSize());
        assertFalse(cache.isObjectPresent(1));
    }

    @Test
    void shouldClearCacheFilesAndIndex() throws IOException {
        cache.putToCache(1, "value");
        cache.clearCache();
        assertEquals(0, cache.getCacheSize());
        try (var files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }

    @Test
    void shouldRejectNegativeCapacityAndForgetEntryWhenDeletionFails() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            try (var ignored = new FileSystemCache<Integer, String>(-1)) {
                // The constructor always throws in this test.
            }
        });
        cache.putToCache(1, "value");
        Path stored = storedFile();
        try (var files = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            files.when(() -> Files.deleteIfExists(stored)).thenThrow(new IOException("locked"));
            cache.removeFromCache(1);
        }
        assertFalse(cache.isObjectPresent(1));
    }

    private Path storedFile() throws IOException {
        try (var files = Files.list(directory)) {
            return files.findFirst().orElseThrow();
        }
    }

    private static final class NonSerializableContent implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @Serial
        private void writeObject(ObjectOutputStream stream) throws IOException {
            throw new NotSerializableException("Intentional test failure");
        }
    }

    private static final class UnreadableContent implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Serial
        private void readObject(ObjectInputStream stream) throws ClassNotFoundException {
            throw new ClassNotFoundException("Simulated unavailable class");
        }
    }
}
