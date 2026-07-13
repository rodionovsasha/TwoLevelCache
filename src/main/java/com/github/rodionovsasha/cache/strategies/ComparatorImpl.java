package com.github.rodionovsasha.cache.strategies;

import lombok.AllArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

@AllArgsConstructor
class ComparatorImpl<K> implements Comparator<K>, Serializable {
    @Serial
    private static final long serialVersionUID = 1;

    private final Map<K, Long> comparatorMap;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public int compare(K key1, K key2) {
        if (key1.equals(key2)) {
            return 0;
        }

        var key1Long = comparatorMap.get(key1);
        var key2Long = comparatorMap.get(key2);

        var valueComparison = key1Long.compareTo(key2Long);
        if (valueComparison != 0) {
            return valueComparison;
        }

        if (key1 instanceof Comparable comparableKey1 && key1.getClass().isInstance(key2)) {
            var keyComparison = comparableKey1.compareTo(key2);
            if (keyComparison != 0) {
                return keyComparison;
            }
        }

        var stringComparison = key1.toString().compareTo(key2.toString());
        if (stringComparison != 0) {
            return stringComparison;
        }

        return Integer.compare(System.identityHashCode(key1), System.identityHashCode(key2));
    }
}
