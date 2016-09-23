package cache.strategies;

import java.util.Map;
import java.util.TreeMap;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public abstract class CacheStrategy<KeyType> {
    final Map<KeyType, Long> objectsStorage;
    final TreeMap<KeyType, Long> sortedObjectsStorage; //TreeMap is sorted by keys.

    CacheStrategy() {
        this.objectsStorage = new TreeMap<>();
        this.sortedObjectsStorage = new TreeMap<>(new ComparatorImpl<>(objectsStorage));
    }

    public abstract void putObject(KeyType key);

    public void removeObject(KeyType key) {
        if(isObjectPresent(key)) {
            objectsStorage.remove(key);
        }
    }

    public boolean isObjectPresent(KeyType key) {
        return objectsStorage.containsKey(key);
    }

    public KeyType getUsedKey() {
        sortedObjectsStorage.putAll(objectsStorage);
        return sortedObjectsStorage.firstKey();
    }

    public void clear() {
        objectsStorage.clear();
    }
}