package com.github.rodionovsasha.cache.strategies;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

/**
 * LRU Strategy - Least Recently Used
 */

public class LRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        objectsStorage().put(key, System.nanoTime());
    }
}
