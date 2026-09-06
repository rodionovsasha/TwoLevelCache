package com.github.rodionovsasha.cache.strategies;

import java.util.Optional;

/** MRU eviction based on access order, without clock-dependent priorities. */
public class MRUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        recordRecency(key);
    }

    @Override
    public Optional<K> selectVictim() {
        return selectByRecency(true);
    }
}
