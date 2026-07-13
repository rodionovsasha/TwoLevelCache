package com.github.rodionovsasha.cache;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

@Slf4j
final class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private final Map<K, String> objectsStorage;
    private final Path tempDir;
    private int capacity;

    FileSystemCache() {
        this.tempDir = createTempDirectory();
        this.tempDir.toFile().deleteOnExit();
        this.objectsStorage = new ConcurrentHashMap<>();
    }

    FileSystemCache(int capacity) {
        this.tempDir = createTempDirectory();
        this.tempDir.toFile().deleteOnExit();
        this.capacity = capacity;
        this.objectsStorage = new ConcurrentHashMap<>(capacity);
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("cache");
        } catch (IOException e) {
            throw new IllegalStateException("Can't create cache temp directory", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V getFromCache(K key) {
        if (isObjectPresent(key)) {
            var fileName = objectsStorage.get(key);
            try (var fileInputStream = new FileInputStream(tempDir + File.separator + fileName);
                 var objectInputStream = new ObjectInputStream(fileInputStream)) {
                return (V) objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                log.error(format("Can't read a file. %s: %s", fileName, e.getMessage()));
            }
        }
        log.debug(format("Object with key '%s' does not exist", key));
        return null;
    }

    @Override
    public synchronized void putToCache(K key, V value) {
        File tmpFile;
        try {
            tmpFile = Files.createTempFile(tempDir, "", "").toFile();
        } catch (IOException e) {
            log.error("Can't create a cache file: " + e.getMessage());
            return;
        }

        try (var outputStream = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            outputStream.writeObject(value);
            outputStream.flush();
            var replacedFileName = objectsStorage.put(key, tmpFile.getName());
            deleteFile(replacedFileName);
        } catch (IOException e) {
            log.error("Can't write an object to a file " + tmpFile.getName() + ": " + e.getMessage());
            deleteFile(tmpFile.getName());
        }
    }

    private void deleteFile(String fileName) {
        if (fileName == null) {
            return;
        }

        var deletedFile = new File(tempDir + File.separator + fileName);
        if (deletedFile.delete()) {
            log.debug(format("Cache file '%s' has been deleted", fileName));
        } else {
            log.debug(format("Can't delete a file %s", fileName));
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
    public int getCacheSize() {
        return objectsStorage.size();
    }

    @Override
    public boolean isObjectPresent(K key) {
        return objectsStorage.containsKey(key);
    }

    @Override
    public boolean hasEmptyPlace() {
        return getCacheSize() < this.capacity;
    }

    @Override
    public void clearCache() {
        try (var files = Files.walk(tempDir)) {
            files.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (file.delete()) {
                            log.debug(format("Cache file '%s' has been deleted", file));
                        } else {
                            log.error(format("Can't delete a file %s", file));
                        }
                    });
        } catch (IOException e) {
            log.error("Can't clear cache directory " + tempDir + ": " + e.getMessage());
        }
        objectsStorage.clear();
    }
}
