package cache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

class MemoryCache<KeyType extends Serializable, ValueType extends Serializable> implements ICache<KeyType, ValueType> {
    private final ConcurrentHashMap<KeyType, ValueType> hashMap;
    private int capacity = CacheApp.MAX_CACHE_MEMORY_CAPACITY;

    MemoryCache() {
        this.hashMap = new ConcurrentHashMap<>(this.capacity); // ConcurrentHashMap is thread safe without synchronizing the whole map. Reads can happen very fast while write is done with a lock.
    }

    MemoryCache(int maxCapacity) {
        this.capacity = maxCapacity;
        this.hashMap = new ConcurrentHashMap<>(this.capacity);
    }

    @Override
    public synchronized ValueType getObjectFromCache(KeyType key) {
        if(isObjectPresent(key)) {
            return hashMap.get(key);
        }
        return null;
    }

    @Override
    public synchronized void putObjectIntoCache(KeyType key, ValueType val) {
        hashMap.put(key, val);
    }

    @Override
    public synchronized void removeObjectFromCache(KeyType key) {
        if (isObjectPresent(key)) {
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
        hashMap.clear();
    }
}