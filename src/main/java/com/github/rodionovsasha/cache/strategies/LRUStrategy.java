package com.github.rodionovsasha.cache.strategies;

import java.util.Optional;

/** LRU eviction based on access order, without clock-dependent priorities. */
public class LRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        recordRecency(key);
    }

    @Override
    public Optional<K> selectVictim() {
        return selectByRecency(false);
    }
}
