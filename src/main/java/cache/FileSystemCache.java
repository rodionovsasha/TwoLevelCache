package cache;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

@Slf4j
class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private static final String CACHE_DIR = "cache";
    private final ConcurrentHashMap<K, String> objectsStorage;
    private int capacity;

    FileSystemCache() {
        createDirectory();
        this.objectsStorage = new ConcurrentHashMap<>();
    }

    FileSystemCache(int capacity) {
        createDirectory();
        this.capacity = capacity;
        this.objectsStorage = new ConcurrentHashMap<>(capacity);
    }

    private void createDirectory() {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            log.debug(format("Creating directory: %s...", cacheDir));
            if (cacheDir.mkdir()) {
                log.debug(format("%s has been created.", cacheDir));
            } else {
                log.error(format("Can't create a directory %s", cacheDir));
            }
        }
    }

    @Override
    public synchronized V getObjectFromCache(K key) {
        if (isObjectPresent(key)) {
            String fileName = objectsStorage.get(key);
            try (FileInputStream fileInputStream = new FileInputStream(new File(CACHE_DIR + File.separator + fileName));
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                return (V)objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                log.error(format("Can't read a file. %s: %s", fileName, e));
            }
        }
        log.debug(format("Object with key '%s' does not exist", key));
        return null;
    }

    @Override
    public synchronized void putObjectIntoCache(K key, V value) {
        String fileName = UUID.randomUUID().toString();

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(CACHE_DIR + File.separator + fileName));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            objectsStorage.put(key, fileName);
        } catch (IOException e) {
            log.error("Can't write an object to a file " + fileName + ": " + e);
        }
    }

    @Override
    public synchronized void removeObjectFromCache(K key) {
        String fileName = objectsStorage.get(key);
        File deletedFile = new File(CACHE_DIR + File.separator + fileName);
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

    @Override
    public void clearCache() {
        File cacheDir = new File(CACHE_DIR);// delete all files in directory (but not a directory)
        if (cacheDir.exists()) {
            Optional<File[]> files = Optional.ofNullable(cacheDir.listFiles());

            if (!files.isPresent()) {
                return;
            }
            for (File file : files.get()) { // remove cache files
                if (file.delete()) {
                    log.debug(format("Cache file '%s' has been deleted", file));
                } else {
                    log.error(format("Can't delete a file %s", file));
                }
            }
        }
        objectsStorage.clear();
    }
}
