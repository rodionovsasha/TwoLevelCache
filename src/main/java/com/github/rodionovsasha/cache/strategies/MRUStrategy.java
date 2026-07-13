package com.github.rodionovsasha.cache.strategies;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

/**
 * MRU Strategy - Most Recently Used
 */

public class MRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        objectsStorage().put(key, System.nanoTime());
    }

    @Override
    public K getReplacedKey() {
        sortedObjectsStorage().clear();
        sortedObjectsStorage().putAll(objectsStorage());
        return sortedObjectsStorage().lastKey();
    }
}
