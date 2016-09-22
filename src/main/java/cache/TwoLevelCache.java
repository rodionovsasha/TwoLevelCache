package cache;

import cache.strategies.CacheStrategy;
import cache.strategies.LFUStrategy;
import cache.strategies.LRUStrategy;
import cache.strategies.MRUStrategy;
import java.io.Serializable;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public class TwoLevelCache<KeyType extends Serializable, ValueType extends Serializable> implements ICache<KeyType, ValueType>{
    private final ICache<KeyType, ValueType> firstLevelCache;
    private final ICache<KeyType, ValueType> secondLevelCache;
    private CacheStrategy<KeyType> strategy = getStrategy(CacheApp.StrategyType.LFU); //default strategy is LFU 

    public TwoLevelCache(ICache<KeyType, ValueType> memoryCache, ICache<KeyType, ValueType> fileSystemCache, CacheApp.StrategyType strategyType) {
        this.strategy = getStrategy(strategyType);
        this.firstLevelCache = memoryCache;
        this.secondLevelCache = fileSystemCache;
    }

    public TwoLevelCache(int memoryCapacity, int fileCapacity, CacheApp.StrategyType strategyType) {
        this.strategy = getStrategy(strategyType);
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
    }

    public TwoLevelCache(int memoryCapacity, int fileCapacity) {
        this.firstLevelCache = new MemoryCache<>(memoryCapacity);
        this.secondLevelCache = new FileSystemCache<>(fileCapacity);
    }
   
    private CacheStrategy<KeyType> getStrategy(CacheApp.StrategyType strategyType) {
        switch (strategyType) {
            case LFU:
                return new LFUStrategy<>();
            case LRU:
                return new LRUStrategy<>();
            case MRU:
                return new MRUStrategy<>();
            default:
                return new LFUStrategy<>();
        }
    }

    @Override
    public synchronized void putObjectIntoCache(KeyType key, ValueType val) {
        if (firstLevelCache.isObjectPresent(key) || firstLevelCache.hasEmptyPlace()) {
            firstLevelCache.putObjectIntoCache(key, val);
        } else if (secondLevelCache.isObjectPresent(key) || secondLevelCache.hasEmptyPlace()) {
            secondLevelCache.putObjectIntoCache(key, val);
        } else {
            // Here we have full cache and have to replace object according to cache strategy.
            replaceUsedKey(key, val);
        }

        if (!strategy.isObjectPresent(key)) {
            strategy.putObject(key);
        }
    }

    private void replaceUsedKey(KeyType key, ValueType val) {
        KeyType usedKey = strategy.getUsedKey();
        if (firstLevelCache.isObjectPresent(usedKey)) {
            firstLevelCache.removeObjectFromCache(usedKey);
            firstLevelCache.putObjectIntoCache(key, val);
        } else {
            secondLevelCache.removeObjectFromCache(usedKey);
            secondLevelCache.putObjectIntoCache(key, val);
        }
    }

    @Override
    public synchronized ValueType getObjectFromCache(KeyType key) {
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
    public synchronized void removeObjectFromCache(KeyType key) {
        if (firstLevelCache.isObjectPresent(key)) {
            firstLevelCache.removeObjectFromCache(key);
        }
        if (secondLevelCache.isObjectPresent(key)) {
            secondLevelCache.removeObjectFromCache(key);
        }
        strategy.removeObject(key);
    }

    @Override
    public int getCacheSize() {
        return (firstLevelCache.getCacheSize() + secondLevelCache.getCacheSize());
    }

    @Override
    public boolean isObjectPresent(KeyType key) {
        return (firstLevelCache.isObjectPresent(key) || secondLevelCache.isObjectPresent(key));
    }

    @Override
    public void clearCache() {
        firstLevelCache.clearCache();
        secondLevelCache.clearCache();
        strategy.clear();
    }

    @Override
    public synchronized boolean hasEmptyPlace() {
        return (firstLevelCache.hasEmptyPlace() || secondLevelCache.hasEmptyPlace());
    }

    ICache<KeyType, ValueType> getFirstLevelCache() {
        return firstLevelCache;
    }

    ICache<KeyType, ValueType> getSecondLevelCache() {
        return secondLevelCache;
    }

    CacheStrategy<KeyType> getStrategy() {
        return strategy;
    }
}