package com.github.rodionovsasha.cache.strategies;

import java.util.Objects;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

/**
 * LFU Strategy - Least Frequently Used
 */

public class LFUStrategy<K> extends CacheStrategy<K> {
    @Override
    public void putObject(K key) {
        Objects.requireNonNull(key, "key");
        long frequency = 1;
        if (objectsStorage().containsKey(key)) {
            long previous = objectsStorage().get(key);
            frequency = previous == Long.MAX_VALUE ? previous : previous + 1;
        }
        objectsStorage().put(key, frequency);
    }
}
