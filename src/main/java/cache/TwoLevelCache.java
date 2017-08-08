package cache;

import cache.strategies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

public class TwoLevelCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwoLevelCache.class);
    private final MemoryCache<K, V> firstLevelCache;
    private final FileSystemCache<K, V> secondLevelCache;
    private final CacheStrategy<K> strategy;

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity, final StrategyType strategyType) {
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
        this.strategy = getStrategy(strategyType);
    }

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity) {
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
        this.strategy = getStrategy(StrategyType.LFU);
    }

    public MemoryCache<K, V> getFirstLevelCache() {
        return firstLevelCache;
    }

    public FileSystemCache<K, V> getSecondLevelCache() {
        return secondLevelCache;
    }

    public CacheStrategy<K> getStrategy() {
        return strategy;
    }

    private CacheStrategy<K> getStrategy(StrategyType strategyType) {
        switch (strategyType) {
        case LRU:
            return new LRUStrategy<>();
        case MRU:
            return new MRUStrategy<>();
        case LFU:
        default:
            return new LFUStrategy<>();
        }
    }

    @Override
    public synchronized void putObjectIntoCache(K newKey, V newValue) {
        if (firstLevelCache.isObjectPresent(newKey) || firstLevelCache.hasEmptyPlace()) {
            LOGGER.debug(format("Put object with key %s to the 1st level", newKey));
            firstLevelCache.putObjectIntoCache(newKey, newValue);
            if (secondLevelCache.isObjectPresent(newKey)) {
                secondLevelCache.removeObjectFromCache(newKey);
            }
        } else if (secondLevelCache.isObjectPresent(newKey) || secondLevelCache.hasEmptyPlace()) {
            LOGGER.debug(format("Put object with key %s to the 2nd level", newKey));
            secondLevelCache.putObjectIntoCache(newKey, newValue);
        } else {
            // Here we have full cache and have to replace some object with new one according to cache strategy.
            replaceObject(newKey, newValue);
        }

        if (!strategy.isObjectPresent(newKey)) {
            LOGGER.debug(format("Put object with key %s to strategy", newKey));
            strategy.putObject(newKey);
        }
    }

    private void replaceObject(K key, V value) {
        K replacedKey = strategy.getReplacedKey();
        if (firstLevelCache.isObjectPresent(replacedKey)) {
            LOGGER.debug(format("Replace object with key %s from 1st level", replacedKey));
            firstLevelCache.removeObjectFromCache(replacedKey);
            firstLevelCache.putObjectIntoCache(key, value);
        } else if (secondLevelCache.isObjectPresent(replacedKey)) {
            LOGGER.debug(format("Replace object with key %s from 2nd level", replacedKey));
            secondLevelCache.removeObjectFromCache(replacedKey);
            secondLevelCache.putObjectIntoCache(key, value);
        }
    }

    @Override
    public synchronized V getObjectFromCache(K key) {
        if (firstLevelCache.isObjectPresent(key)) {
            strategy.putObject(key);
            return firstLevelCache.getObjectFromCache(key);
        } else if (secondLevelCache.isObjectPresent(key)) {
            strategy.putObject(key);
            return secondLevelCache.getObjectFromCache(key);
        }
        return null;
    }

    @Override
    public synchronized void removeObjectFromCache(K key) {
        if (firstLevelCache.isObjectPresent(key)) {
            LOGGER.debug(format("Remove object with key %s from 1st level", key));
            firstLevelCache.removeObjectFromCache(key);
        }
        if (secondLevelCache.isObjectPresent(key)) {
            LOGGER.debug(format("Remove object with key %s from 2nd level", key));
            secondLevelCache.removeObjectFromCache(key);
        }
        strategy.removeObject(key);
    }

    @Override
    public int getCacheSize() {
        return firstLevelCache.getCacheSize() + secondLevelCache.getCacheSize();
    }

    @Override
    public boolean isObjectPresent(K key) {
        return firstLevelCache.isObjectPresent(key) || secondLevelCache.isObjectPresent(key);
    }

    @Override
    public void clearCache() {
        firstLevelCache.clearCache();
        secondLevelCache.clearCache();
        strategy.clear();
    }

    @Override
    public synchronized boolean hasEmptyPlace() {
        return firstLevelCache.hasEmptyPlace() || secondLevelCache.hasEmptyPlace();
    }
}
