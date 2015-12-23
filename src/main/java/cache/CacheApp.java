package cache;

import java.io.Serializable;

public class CacheApp<KeyType extends Serializable, ValueType extends Serializable> {
    public static final int MAX_CACHE_MEMORY_CAPACITY = 3; // maximum number of entries in the cache memory. If the number of items exceeds this number - called eviction algorithm (move to fileSystem).
    public static final int MAX_CACHE_FILE_CAPACITY = 10;  // maximum number of entries in the cache fileSystem. If the number of items exceeds this number - remove object from cache.
    public enum StrategyType {LFU, LRU, MRU}

    private void run() {
        ICache<KeyType, ValueType> memoryCache = new MemoryCache<KeyType, ValueType>(MAX_CACHE_MEMORY_CAPACITY);
        ICache<KeyType, ValueType> fileSystemCache = new FileSystemCache<KeyType, ValueType>(MAX_CACHE_FILE_CAPACITY);
        new TwoLevelCache<KeyType, ValueType>(memoryCache, fileSystemCache, StrategyType.LFU);
    }

    public static void main(String[] args){
        new CacheApp().run();
    }
}