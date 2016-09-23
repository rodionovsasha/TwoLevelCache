package cache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

class MemoryCache<KeyType extends Serializable, ValueType extends Serializable> implements ICache<KeyType, ValueType> {
    private final ConcurrentHashMap<KeyType, ValueType> objectsStorage;
    private int capacity = CacheApp.MAX_CACHE_MEMORY_CAPACITY;

    MemoryCache() {
        this.objectsStorage = new ConcurrentHashMap<>(this.capacity); // ConcurrentHashMap is thread safe without synchronizing the whole map. Reads can happen very fast while write is done with a lock.
    }

    MemoryCache(int maxCapacity) {
        this.capacity = maxCapacity;
        this.objectsStorage = new ConcurrentHashMap<>(this.capacity);
    }

    @Override
    public synchronized ValueType getObjectFromCache(KeyType objectKey) {
        if(isObjectPresent(objectKey)) {
            return objectsStorage.get(objectKey);
        }
        return null;
    }

    @Override
    public synchronized void putObjectIntoCache(KeyType objectKey, ValueType objectValue) {
        objectsStorage.put(objectKey, objectValue);
    }

    @Override
    public synchronized void removeObjectFromCache(KeyType objectKey) {
        if (isObjectPresent(objectKey)) {
            objectsStorage.remove(objectKey);
        }
    }

    @Override
    public int getCacheSize() {
        return objectsStorage.size();
    }

    @Override
    public boolean isObjectPresent(KeyType objectKey) {
        return objectsStorage.containsKey(objectKey);
    }

    @Override
    public boolean hasEmptyPlace() {
        return (getCacheSize() < this.capacity);
    }

    @Override
    public void clearCache() {
        objectsStorage.clear();
    }
}