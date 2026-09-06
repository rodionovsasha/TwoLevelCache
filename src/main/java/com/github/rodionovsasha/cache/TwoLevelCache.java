package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.CacheStrategy;
import com.github.rodionovsasha.cache.strategies.LFUStrategy;
import com.github.rodionovsasha.cache.strategies.LRUStrategy;
import com.github.rodionovsasha.cache.strategies.MRUStrategy;
import com.github.rodionovsasha.cache.strategies.StrategyType;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

@Slf4j
public class TwoLevelCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
    private final MemoryCache<K, V> firstLevelCache;
    private final FileSystemCache<K, V> secondLevelCache;
    private final CacheStrategy<K> strategy;

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity, final StrategyType strategyType) {
        validateCapacity(memoryCapacity, fileCapacity);
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
        this.strategy = getStrategy(strategyType);
    }

    public TwoLevelCache(final int memoryCapacity, final int fileCapacity) {
        validateCapacity(memoryCapacity, fileCapacity);
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
        this.strategy = getStrategy(StrategyType.LFU);
    }

    private static void validateCapacity(final int memoryCapacity, final int fileCapacity) {
        if (memoryCapacity < 0 || fileCapacity < 0) {
            throw new IllegalArgumentException("Cache capacities must not be negative");
        }
        if ((long) memoryCapacity + fileCapacity == 0) {
            throw new IllegalArgumentException("Total cache capacity must be greater than zero");
        }
    }

    MemoryCache<K, V> getFirstLevelCache() {
        return firstLevelCache;
    }

    FileSystemCache<K, V> getSecondLevelCache() {
        return secondLevelCache;
    }

    CacheStrategy<K> getStrategy() {
        return strategy;
    }

    private CacheStrategy<K> getStrategy(StrategyType strategyType) {
        return switch (strategyType) {
            case LRU -> new LRUStrategy<>();
            case MRU -> new MRUStrategy<>();
            default -> new LFUStrategy<>();
        };
    }

    @Override
    public synchronized void putToCache(K newKey, V newValue) {
        if (firstLevelCache.isObjectPresent(newKey) || firstLevelCache.hasEmptyPlace()) {
            log.debug(format("Put object with key %s to the 1st level", newKey));
            firstLevelCache.putToCache(newKey, newValue);
            if (secondLevelCache.isObjectPresent(newKey)) {
                secondLevelCache.removeFromCache(newKey);
            }
        } else if (secondLevelCache.isObjectPresent(newKey) || secondLevelCache.hasEmptyPlace()) {
            log.debug(format("Put object with key %s to the 2nd level", newKey));
            secondLevelCache.putToCache(newKey, newValue);
        } else {
            // Here we have full cache and have to replace some object with new one according to cache strategy.
            replaceObject(newKey, newValue);
        }

        log.debug(format("Put object with key %s to strategy", newKey));
        strategy.onInsert(newKey);
    }

    private void replaceObject(K key, V value) {
        var replacedKey = strategy.getReplacedKey();
        if (firstLevelCache.isObjectPresent(replacedKey)) {
            log.debug(format("Replace object with key %s from 1st level", replacedKey));
            firstLevelCache.removeFromCache(replacedKey);
            firstLevelCache.putToCache(key, value);
        } else if (secondLevelCache.isObjectPresent(replacedKey)) {
            log.debug(format("Replace object with key %s from 2nd level", replacedKey));
            secondLevelCache.removeFromCache(replacedKey);
            secondLevelCache.putToCache(key, value);
        }
        strategy.removeObject(replacedKey);
    }

    @Override
    public synchronized V getFromCache(K key) {
        if (firstLevelCache.isObjectPresent(key)) {
            strategy.onAccess(key);
            return firstLevelCache.getFromCache(key);
        } else if (secondLevelCache.isObjectPresent(key)) {
            strategy.onAccess(key);
            return secondLevelCache.getFromCache(key);
        }
        return null;
    }

    @Override
    public synchronized void removeFromCache(K key) {
        if (firstLevelCache.isObjectPresent(key)) {
            log.debug(format("Remove object with key %s from 1st level", key));
            firstLevelCache.removeFromCache(key);
        }
        if (secondLevelCache.isObjectPresent(key)) {
            log.debug(format("Remove object with key %s from 2nd level", key));
            secondLevelCache.removeFromCache(key);
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
    public synchronized void clearCache() {
        firstLevelCache.clearCache();
        secondLevelCache.clearCache();
        strategy.clear();
    }

    @Override
    public synchronized boolean hasEmptyPlace() {
        return firstLevelCache.hasEmptyPlace() || secondLevelCache.hasEmptyPlace();
    }
}
