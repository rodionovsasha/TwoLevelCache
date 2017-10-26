package com.github.rodionovsasha.cache;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

@Slf4j
class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private final Map<K, String> objectsStorage;
    private final Path tempDir;
    private int capacity;

    @SneakyThrows
    FileSystemCache() {
        this.tempDir = Files.createTempDirectory("cache");
        this.tempDir.toFile().deleteOnExit();
        this.objectsStorage = new ConcurrentHashMap<>();
    }

    @SneakyThrows
    FileSystemCache(int capacity) {
        this.tempDir = Files.createTempDirectory("cache");
        this.tempDir.toFile().deleteOnExit();
        this.capacity = capacity;
        this.objectsStorage = new ConcurrentHashMap<>(capacity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized V getFromCache(K key) {
        if (isObjectPresent(key)) {
            val fileName = objectsStorage.get(key);
            try (val fileInputStream = new FileInputStream(new File(tempDir + File.separator + fileName));
                 val objectInputStream = new ObjectInputStream(fileInputStream)) {
                return (V) objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                log.error(format("Can't read a file. %s: %s", fileName, e.getMessage()));
            }
        }
        log.debug(format("Object with key '%s' does not exist", key));
        return null;
    }

    @Override
    @SneakyThrows
    public synchronized void putToCache(K key, V value) {
        val tmpFile = Files.createTempFile(tempDir, "", "").toFile();

        try (val outputStream = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            outputStream.writeObject(value);
            outputStream.flush();
            objectsStorage.put(key, tmpFile.getName());
        } catch (IOException e) {
            log.error("Can't write an object to a file " + tmpFile.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public synchronized void removeFromCache(K key) {
        val fileName = objectsStorage.get(key);
        val deletedFile = new File(tempDir + File.separator + fileName);
        if (deletedFile.delete()) {
            log.debug(format("Cache file '%s' has been deleted", fileName));
        } else {
            log.debug(format("Can't delete a file %s", fileName));
        }
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

    @SneakyThrows
    @Override
    public void clearCache() {
        Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(file -> {
                    if (file.delete()) {
                        log.debug(format("Cache file '%s' has been deleted", file));
                    } else {
                        log.error(format("Can't delete a file %s", file));
                    }
                });
        objectsStorage.clear();
    }
}
