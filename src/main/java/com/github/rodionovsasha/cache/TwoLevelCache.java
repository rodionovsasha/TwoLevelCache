package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.CacheStrategy;
import com.github.rodionovsasha.cache.strategies.LFUStrategy;
import com.github.rodionovsasha.cache.strategies.LRUStrategy;
import com.github.rodionovsasha.cache.strategies.MRUStrategy;
import com.github.rodionovsasha.cache.strategies.StrategyType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;

import static java.lang.String.format;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

@Slf4j
@Getter
public class TwoLevelCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {
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

        if (!strategy.isObjectPresent(newKey)) {
            log.debug(format("Put object with key %s to strategy", newKey));
            strategy.putObject(newKey);
        }
    }

    private void replaceObject(K key, V value) {
        val replacedKey = strategy.getReplacedKey();
        if (firstLevelCache.isObjectPresent(replacedKey)) {
            log.debug(format("Replace object with key %s from 1st level", replacedKey));
            firstLevelCache.removeFromCache(replacedKey);
            firstLevelCache.putToCache(key, value);
        } else if (secondLevelCache.isObjectPresent(replacedKey)) {
            log.debug(format("Replace object with key %s from 2nd level", replacedKey));
            secondLevelCache.removeFromCache(replacedKey);
            secondLevelCache.putToCache(key, value);
        }
    }

    @Override
    public synchronized V getFromCache(K key) {
        if (firstLevelCache.isObjectPresent(key)) {
            strategy.putObject(key);
            return firstLevelCache.getFromCache(key);
        } else if (secondLevelCache.isObjectPresent(key)) {
            strategy.putObject(key);
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
