package cache.strategies;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

/**
 * LFU Strategy - Least Frequently Used
 */

public class LFUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        long frequency = 1;
        if (objectsStorage.containsKey(key)) {
            frequency = objectsStorage.get(key) + 1;
        }
        objectsStorage.put(key, frequency);
    }
}