package com.github.rodionovsasha.cache;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

@Slf4j
final class FileSystemCache<K, V extends Serializable> implements Cache<K, V>, AutoCloseable {
    private final Map<K, String> objectsStorage;
    private final Path storageDirectory;
    private final int capacity;

    FileSystemCache() {
        this(Integer.MAX_VALUE, null);
    }

    FileSystemCache(int capacity) {
        this(capacity, null);
    }

    FileSystemCache(int capacity, Path cacheRoot) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Cache capacity must not be negative");
        }
        this.capacity = capacity;
        this.objectsStorage = new ConcurrentHashMap<>(Math.min(capacity, 16));
        this.storageDirectory = createStorageDirectory(cacheRoot);
    }

    private static Path createStorageDirectory(Path cacheRoot) {
        try {
            if (cacheRoot == null) {
                return Files.createTempDirectory("two-level-cache-");
            }
            Files.createDirectories(cacheRoot);
            return Files.createTempDirectory(cacheRoot, "two-level-cache-");
        } catch (IOException e) {
            throw new IllegalStateException("Can't create cache temp directory", e);
        }
    }

    Path getStorageDirectory() {
        return storageDirectory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V getFromCache(K key) {
        var fileName = objectsStorage.get(key);
        if (fileName == null) {
            return null;
        }
        try (var inputStream = Files.newInputStream(storageDirectory.resolve(fileName));
             var objectInputStream = new ObjectInputStream(inputStream)) {
            return (V) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            log.error(format("Can't read cache file %s: %s", fileName, e.getMessage()));
            removeFromCache(key);
            return null;
        }
    }

    synchronized boolean store(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Path temporaryFile;
        try {
            temporaryFile = Files.createTempFile(storageDirectory, "entry-", ".bin");
        } catch (IOException e) {
            log.error("Can't create a cache file: " + e.getMessage());
            return false;
        }

        try (var outputStream = new ObjectOutputStream(
                Files.newOutputStream(temporaryFile, StandardOpenOption.WRITE))) {
            outputStream.writeObject(value);
            outputStream.flush();
            var replacedFileName = objectsStorage.put(key, temporaryFile.getFileName().toString());
            deleteFile(replacedFileName);
            return true;
        } catch (IOException e) {
            log.error("Can't write an object to a file " + temporaryFile.getFileName() + ": " + e.getMessage());
            deletePath(temporaryFile);
            return false;
        }
    }

    @Override
    public synchronized void putToCache(K key, V value) {
        store(key, value);
    }

    private void deleteFile(String fileName) {
        if (fileName == null) {
            return;
        }

        deletePath(storageDirectory.resolve(fileName));
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn(format("Can't delete cache file %s: %s", path, e.getMessage()));
        }
    }

    @Override
    public synchronized void removeFromCache(K key) {
        var fileName = objectsStorage.get(key);
        if (fileName == null) {
            log.debug(format("Object with key '%s' does not exist", key));
            return;
        }

        deleteFile(fileName);
        objectsStorage.remove(key);
    }

    @Override
    public synchronized int getCacheSize() {
        return objectsStorage.size();
    }

    @Override
    public synchronized boolean isObjectPresent(K key) {
        return objectsStorage.containsKey(key);
    }

    @Override
    public synchronized boolean hasEmptyPlace() {
        return getCacheSize() < this.capacity;
    }

    @Override
    public synchronized void clearCache() {
        try (var files = Files.walk(storageDirectory)) {
            files.filter(Files::isRegularFile)
                    .forEach(this::deletePath);
        } catch (IOException e) {
            log.error("Can't clear cache directory " + storageDirectory + ": " + e.getMessage());
        }
        objectsStorage.clear();
    }

    synchronized Set<K> keys() {
        return Set.copyOf(objectsStorage.keySet());
    }

    @Override
    public synchronized void close() {
        clearCache();
        deletePath(storageDirectory);
    }
}
