package cache.strategies;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

/**
 * LFU (англ.) (англ. Least Frequently Used - Наименее часто используемый) — вытесняется буфер, использованный реже всех;
 * LFU подсчитывает как часто используется элемент. Те элементы, обращения к которым происходят реже всего, вытесняются в первую очередь.
 */

public class LFUStrategy<KeyType> extends CacheStrategy<KeyType> {
    @Override
    public void putObject(KeyType key) {
        long frequency = 1;
        if (treeMap.containsKey(key)) {
            frequency = treeMap.get(key) + 1;
        }
        treeMap.put(key, frequency);
    }
}