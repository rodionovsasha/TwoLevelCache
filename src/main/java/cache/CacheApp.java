package cache;

import java.io.Serializable;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public class CacheApp<KeyType extends Serializable, ValueType extends Serializable> {
    static final int MAX_CACHE_MEMORY_CAPACITY = 3; // maximum number of entries in the cache memory. If the number of items exceeds this number - called eviction algorithm (move to fileSystem).
    static final int MAX_CACHE_FILE_CAPACITY = 10;  // maximum number of entries in the cache fileSystem. If the number of items exceeds this number - remove object from cache.
    enum StrategyType {LFU, LRU, MRU}

    private void run() {
        ICache<KeyType, ValueType> memoryCache = new MemoryCache<>(MAX_CACHE_MEMORY_CAPACITY);
        ICache<KeyType, ValueType> fileSystemCache = new FileSystemCache<>(MAX_CACHE_FILE_CAPACITY);
        new TwoLevelCache<>(memoryCache, fileSystemCache, StrategyType.LFU);
    }

    public static void main(String[] args){
        new CacheApp().run();
    }
}