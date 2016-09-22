package cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

class FileSystemCache<KeyType extends Serializable, ValueType extends Serializable> implements ICache<KeyType, ValueType> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemCache.class);
    private static final String CACHE_DIR = "cache";
    private final ConcurrentHashMap<KeyType, String> hashMap;
    private int capacity = CacheApp.MAX_CACHE_FILE_CAPACITY;

    FileSystemCache() {
        createDirectory();
        this.hashMap = new ConcurrentHashMap<>(this.capacity);
    }

    FileSystemCache(int maxCapacity) {
        createDirectory();
        this.capacity = maxCapacity;
        this.hashMap = new ConcurrentHashMap<>(this.capacity);
    }

    private void createDirectory() {
        File cacheDir = new File(CACHE_DIR);
        if(!cacheDir.exists()) {
            LOGGER.info("Creating directory: " + cacheDir + "...");
            if(cacheDir.mkdir()) {
                LOGGER.info(cacheDir + " has been created.");
            } else {
                LOGGER.error("Can't create a directory " + cacheDir);
            }
        }
    }

    @Override
    public synchronized ValueType getObjectFromCache(KeyType key) {
        if(isObjectPresent(key)) {
            String fileName = hashMap.get(key);
            ObjectInputStream objectInputStream = null;
            try {
                FileInputStream fileInput = new FileInputStream(new File(CACHE_DIR + "/" + fileName));
                objectInputStream = new ObjectInputStream(fileInput);
                return (ValueType) objectInputStream.readObject();
            } catch (Exception e){
                LOGGER.error("Can't read a file." + fileName + ": " + e);
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        LOGGER.error("Can't close inputStream: " + e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void putObjectIntoCache(KeyType key, ValueType val) {
        String fileName = UUID.randomUUID().toString();
        ObjectOutputStream objectOutputStream = null;
        try {            
            FileOutputStream fileStream = new FileOutputStream(new File(CACHE_DIR + "/" + fileName));
            objectOutputStream = new ObjectOutputStream(fileStream);
            objectOutputStream.writeObject(val);
            objectOutputStream.flush();
            hashMap.put(key, fileName);
        } catch (Exception e) {
            LOGGER.error("Can't write an object to a file " + fileName + ": " + e);
        } finally {
            if (objectOutputStream != null){
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Can't close outputStream: " + e);
                }
            }
        }
    }

    @Override
    public synchronized void removeObjectFromCache(KeyType key) {
        if (isObjectPresent(key)) {
            String fileName = hashMap.get(key);
            File deletingFile = new File(CACHE_DIR + "/" + fileName);
            if(deletingFile.delete()) {// remove cache file
                LOGGER.info("Cache file '" + fileName + "' has been deleted");
            } else {
                LOGGER.info("Can't delete a file " + fileName);
            }
            hashMap.remove(key);
        }
    }

    @Override
    public int getCacheSize() {
        return hashMap.size();
    }

    @Override
    public boolean isObjectPresent(KeyType key) {
        return hashMap.containsKey(key);
    }

    @Override
    public boolean hasEmptyPlace() {
        return (getCacheSize() < this.capacity);
    }

    @Override
    public void clearCache() {
        File cacheDir = new File(CACHE_DIR);// delete all files in directory (but not directory)
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files == null) {
                return;
            }
            for(File file : files) {// remove cache files
                if(file.delete()) {
                    LOGGER.info("Cache file '" + file + "' has been deleted");
                } else {
                    LOGGER.error("Can't delete a file " + file);
                }
            }
        }
        hashMap.clear();
    }
}