package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.CacheStrategy;
import com.github.rodionovsasha.cache.strategies.LFUStrategy;
import com.github.rodionovsasha.cache.strategies.LRUStrategy;
import com.github.rodionovsasha.cache.strategies.MRUStrategy;
import com.github.rodionovsasha.cache.strategies.StrategyType;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

/**
 * A read-through cache: L1 stores hot objects in memory and L2 stores serialized objects on disk.
 * A hit in L2 is promoted to L1; eviction from L1 leaves the L2 copy intact.
 */
@Slf4j
public class TwoLevelCache<K, V extends Serializable> implements Cache<K, V>, AutoCloseable {
    private final MemoryCache<K, V> firstLevelCache;
    private final FileSystemCache<K, V> secondLevelCache;
    private final CacheStrategy<K> firstLevelStrategy;
    private final CacheStrategy<K> secondLevelStrategy;

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity, final StrategyType strategyType) {
        this(new TwoLevelCacheConfig(memoryCapacity, fileCapacity, strategyType, strategyType));
    }

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity) {
        this(memoryCapacity, fileCapacity, StrategyType.LFU);
    }

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity,
                         final StrategyType memoryStrategy, final StrategyType fileStrategy) {
        this(new TwoLevelCacheConfig(memoryCapacity, fileCapacity, memoryStrategy, fileStrategy));
    }

    public TwoLevelCache(final TwoLevelCacheConfig config) {
        Objects.requireNonNull(config, "config");
        this.firstLevelCache = new MemoryCache<>(config.memoryCapacity());
        this.secondLevelCache = new FileSystemCache<>(config.fileCapacity(), config.fileCacheRoot());
        this.firstLevelStrategy = createStrategy(config.memoryStrategy());
        this.secondLevelStrategy = createStrategy(config.fileStrategy());
    }

    MemoryCache<K, V> getFirstLevelCache() {
        return firstLevelCache;
    }

    FileSystemCache<K, V> getSecondLevelCache() {
        return secondLevelCache;
    }

    CacheStrategy<K> getFirstLevelStrategy() {
        return firstLevelStrategy;
    }

    CacheStrategy<K> getSecondLevelStrategy() {
        return secondLevelStrategy;
    }

    /** @deprecated use {@link #getFirstLevelStrategy()}. */
    @Deprecated
    CacheStrategy<K> getStrategy() {
        return firstLevelStrategy;
    }

    private static <K> CacheStrategy<K> createStrategy(StrategyType strategyType) {
        return switch (Objects.requireNonNull(strategyType, "strategyType")) {
            case LRU -> new LRUStrategy<>();
            case MRU -> new MRUStrategy<>();
            case LFU -> new LFUStrategy<>();
        };
    }

    /**
     * Stores the value in every enabled level. Returns false only when neither level accepts it.
     * A disk failure can leave the value available in memory until that L1 entry is evicted.
     */
    public synchronized boolean put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        boolean diskStored = putIntoSecondLevel(key, value);
        boolean memoryStored = putIntoFirstLevel(key, value);
        if (!diskStored && secondLevelCache.isObjectPresent(key)) {
            secondLevelCache.removeFromCache(key);
            secondLevelStrategy.removeObject(key);
        }
        return memoryStored || diskStored;
    }

    @Override
    public synchronized void putToCache(K key, V value) {
        if (!put(key, value)) {
            log.warn("Object with key {} was not cached", key);
        }
    }

    private boolean putIntoFirstLevel(K key, V value) {
        if (firstLevelCache.isObjectPresent(key)) {
            firstLevelCache.putToCache(key, value);
            firstLevelStrategy.onInsert(key);
            return true;
        }
        if (firstLevelCache.hasEmptyPlace()) {
            firstLevelCache.putToCache(key, value);
            firstLevelStrategy.onInsert(key);
            return true;
        }
        while (true) {
            var victim = firstLevelStrategy.selectVictim();
            if (victim.isEmpty()) {
                return false;
            }
            K victimKey = victim.get();
            if (!firstLevelCache.isObjectPresent(victimKey)) {
                firstLevelStrategy.removeObject(victimKey);
                continue;
            }
            firstLevelCache.removeFromCache(victimKey);
            firstLevelStrategy.removeObject(victimKey);
            firstLevelCache.putToCache(key, value);
            firstLevelStrategy.onInsert(key);
            return true;
        }
    }

    private boolean putIntoSecondLevel(K key, V value) {
        if (secondLevelCache.isObjectPresent(key)) {
            if (!secondLevelCache.store(key, value)) {
                return false;
            }
            secondLevelStrategy.onInsert(key);
            return true;
        }
        if (secondLevelCache.hasEmptyPlace()) {
            if (!secondLevelCache.store(key, value)) {
                return false;
            }
            secondLevelStrategy.onInsert(key);
            return true;
        }
        while (true) {
            var victim = secondLevelStrategy.selectVictim();
            if (victim.isEmpty()) {
                return false;
            }
            K victimKey = victim.get();
            if (!secondLevelCache.isObjectPresent(victimKey)) {
                secondLevelStrategy.removeObject(victimKey);
                continue;
            }
            if (!secondLevelCache.store(key, value)) {
                return false;
            }
            secondLevelCache.removeFromCache(victimKey);
            secondLevelStrategy.removeObject(victimKey);
            secondLevelStrategy.onInsert(key);
            return true;
        }
    }

    /** Returns the value from L1 or promotes a successful L2 hit to L1. */
    public synchronized V get(K key) {
        Objects.requireNonNull(key, "key");
        V memoryValue = firstLevelCache.getFromCache(key);
        if (memoryValue != null) {
            firstLevelStrategy.onAccess(key);
            return memoryValue;
        }
        if (!secondLevelCache.isObjectPresent(key)) {
            return null;
        }
        V fileValue = secondLevelCache.getFromCache(key);
        if (fileValue == null) {
            secondLevelStrategy.removeObject(key);
            return null;
        }
        secondLevelStrategy.onAccess(key);
        putIntoFirstLevel(key, fileValue);
        return fileValue;
    }

    @Override
    public synchronized V getFromCache(K key) {
        return get(key);
    }

    public synchronized void remove(K key) {
        Objects.requireNonNull(key, "key");
        firstLevelCache.removeFromCache(key);
        secondLevelCache.removeFromCache(key);
        firstLevelStrategy.removeObject(key);
        secondLevelStrategy.removeObject(key);
    }

    @Override
    public synchronized void removeFromCache(K key) {
        remove(key);
    }

    @Override
    public synchronized int getCacheSize() {
        var keys = new HashSet<>(firstLevelCache.keys());
        keys.addAll(secondLevelCache.keys());
        return keys.size();
    }

    @Override
    public synchronized boolean isObjectPresent(K key) {
        return firstLevelCache.isObjectPresent(key) || secondLevelCache.isObjectPresent(key);
    }

    @Override
    public synchronized boolean hasEmptyPlace() {
        return firstLevelCache.hasEmptyPlace() || secondLevelCache.hasEmptyPlace();
    }

    @Override
    public synchronized void clearCache() {
        firstLevelCache.clearCache();
        secondLevelCache.clearCache();
        firstLevelStrategy.clear();
        secondLevelStrategy.clear();
    }

    /** Releases disk resources. The cache cannot be used afterwards. */
    @Override
    public synchronized void close() {
        firstLevelCache.clearCache();
        firstLevelStrategy.clear();
        secondLevelStrategy.clear();
        secondLevelCache.close();
    }
}
