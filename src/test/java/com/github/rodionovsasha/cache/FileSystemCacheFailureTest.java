package com.github.rodionovsasha.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileSystemCacheFailureTest {
    private FileSystemCache<Integer, Serializable> cache;
    private Path directory;

    @BeforeEach
    void setUp() throws Exception {
        cache = new FileSystemCache<>(2);
        var field = FileSystemCache.class.getDeclaredField("tempDir");
        field.setAccessible(true);
        directory = (Path) field.get(cache);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(directory)) {
            cache.clearCache();
            Files.delete(directory);
        }
    }

    @Test
    void shouldExposeDirectoryCreationFailureWithOriginalCause() {
        var failure = new IOException("Directory unavailable");
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempDirectory("cache")).thenThrow(failure);
            var exception = assertThrows(IllegalStateException.class, () -> new FileSystemCache<>(1));
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
    }

    @Test
    void shouldReturnMissWhenSerializedClassCannotBeLoaded() {
        cache.putToCache(1, new UnreadableContent());
        assertNull(cache.getFromCache(1));
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
    void shouldClearIndexWhenAFileCannotBeDeleted() throws IOException {
        cache.putToCache(1, "value");
        var stored = storedFile();
        var path = mock(Path.class);
        var file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(file.delete()).thenReturn(false);
        try (var files = mockStatic(Files.class)) {
            files.when(() -> Files.walk(directory)).thenReturn(Stream.of(path));
            files.when(() -> Files.isRegularFile(path)).thenReturn(true);
            cache.clearCache();
            verify(file).delete();
            assertEquals(0, cache.getCacheSize());
        }
        assertTrue(Files.exists(stored));
    }

    private Path storedFile() throws IOException {
        try (var files = Files.list(directory)) {
            return files.findFirst().orElseThrow();
        }
    }

    private static final class NonSerializableContent implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private final Object value = new Object();
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
