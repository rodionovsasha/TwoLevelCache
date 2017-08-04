package cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

class FileSystemCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemCache.class);
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
            LOGGER.debug(format("Creating directory: %s...", cacheDir));
            if (cacheDir.mkdir()) {
                LOGGER.debug(format("%s has been created.", cacheDir));
            } else {
                LOGGER.error(format("Can't create a directory %s", cacheDir));
            }
        }
    }

    @Override
    public synchronized V getObjectFromCache(K key) {
        if (isObjectPresent(key)) {
            String fileName = objectsStorage.get(key);
            try (FileInputStream fileInputStream = new FileInputStream(new File(CACHE_DIR + "/" + fileName));
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                return (V)objectInputStream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                LOGGER.error(format("Can't read a file. %s: %s", fileName, e));
            }
        }
        LOGGER.debug(format("Object with key '%s' does not exist", key));
        return null;
    }

    @Override
    public synchronized void putObjectIntoCache(K key, V value) {
        String fileName = UUID.randomUUID().toString();

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(CACHE_DIR + "/" + fileName));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(value);
            objectOutputStream.flush();
            objectsStorage.put(key, fileName);
        } catch (IOException e) {
            LOGGER.error("Can't write an object to a file " + fileName + ": " + e);
        }
    }

    @Override
    public synchronized void removeObjectFromCache(K key) {
        String fileName = objectsStorage.get(key);
        File deletedFile = new File(CACHE_DIR + "/" + fileName);
        if (deletedFile.delete()) {
            LOGGER.debug(format("Cache file '%s' has been deleted", fileName));
        } else {
            LOGGER.debug(format("Can't delete a file %s", fileName));
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
            File[] files = cacheDir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) { // remove cache files
                if (file.delete()) {
                    LOGGER.debug(format("Cache file '%s' has been deleted", file));
                } else {
                    LOGGER.error(format("Can't delete a file %s", file));
                }
            }
        }
        objectsStorage.clear();
    }
}
