package cache;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

class MemoryCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private final Map<K, V> objectsStorage;
    private final int capacity;

    MemoryCache(int capacity) {
        this.capacity = capacity;
        this.objectsStorage = new ConcurrentHashMap<>(capacity);
    }

    @Override
    public V getObjectFromCache(K key) {
        return objectsStorage.get(key);
    }

    @Override
    public void putObjectIntoCache(K key, V value) {
        objectsStorage.put(key, value);
    }

    @Override
    public void removeObjectFromCache(K key) {
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
        objectsStorage.clear();
    }
}
