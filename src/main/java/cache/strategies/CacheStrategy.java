package cache.strategies;

import java.util.Map;
import java.util.TreeMap;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

public abstract class CacheStrategy<K> {
    private final Map<K, Long> objectsStorage;
    private final TreeMap<K, Long> sortedObjectsStorage;

    CacheStrategy() {
        this.objectsStorage = new TreeMap<>();
        this.sortedObjectsStorage = new TreeMap<>(new ComparatorImpl<>(objectsStorage));
    }

    public abstract void putObject(K key);

    public void removeObject(K key) {
        if (isObjectPresent(key)) {
            objectsStorage.remove(key);
        }
    }

    public boolean isObjectPresent(K key) {
        return objectsStorage.containsKey(key);
    }

    public K getReplacedKey() {
        sortedObjectsStorage.putAll(objectsStorage);
        return sortedObjectsStorage.firstKey();
    }

    public void clear() {
        objectsStorage.clear();
    }

    public Map<K, Long> getObjectsStorage() {
        return objectsStorage;
    }

    public TreeMap<K, Long> getSortedObjectsStorage() {
        return sortedObjectsStorage;
    }
}
