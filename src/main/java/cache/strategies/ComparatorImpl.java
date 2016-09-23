package cache.strategies;

import java.util.Comparator;
import java.util.Map;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

class ComparatorImpl<KeyType> implements Comparator<KeyType> {
    private final Map<KeyType, Long> comparatorMap;

    ComparatorImpl(Map<KeyType, Long> comparatorMap) {
        this.comparatorMap = comparatorMap;
    }

    @Override
    public int compare(KeyType key1, KeyType key2) {
        if(comparatorMap.get(key1) > comparatorMap.get(key2)) {
            return 1;   // key1 must be later than key2
        } else if(comparatorMap.get(key1) < comparatorMap.get(key2)) {
            return -1;  // key1 must be earlier than key2
        } else {
            return 0;   // key1 and key2 are equals
        }
    }
}