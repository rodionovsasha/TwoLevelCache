package cache.strategies;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

/**
 * LRU Strategy - Least Recently Used
 */

public class LRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        getObjectsStorage().put(key, System.nanoTime());
    }
}
