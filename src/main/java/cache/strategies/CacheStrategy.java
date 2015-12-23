package cache.strategies;

import java.util.Map;
import java.util.TreeMap;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public abstract class CacheStrategy<KeyType> {
    final Map<KeyType, Long> treeMap; // A TreeMap is sorted by keys.
    final TreeMap<KeyType, Long> sortedTree;

    CacheStrategy() {
        this.treeMap = new TreeMap<KeyType, Long>();
        this.sortedTree = new TreeMap<KeyType, Long>(new ComparatorImpl<KeyType>(treeMap));
    }

    public abstract void putObject(KeyType key);

    public void removeObject(KeyType key) {
        if(isObjectPresent(key)) {
            treeMap.remove(key);
        }
    }

    public boolean isObjectPresent(KeyType key) {
        return treeMap.containsKey(key);
    }

    public KeyType getUsedKey() {
        sortedTree.putAll(treeMap);
        return sortedTree.firstKey();
    }

    public void clear() {
        treeMap.clear();
    }
}