package cache.strategies;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

/**
 * MRU Strategy - Most Recently Used
 */

public class MRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        getObjectsStorage().put(key, System.nanoTime());
    }

    @Override
    public K getReplacedKey() {
        getSortedObjectsStorage().putAll(getObjectsStorage());
        return getSortedObjectsStorage().lastKey();
    }
}
