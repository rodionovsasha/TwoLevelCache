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
    public void putObject(KeyType objectKey) {
        long frequency = 1;
        if (objectsStorage.containsKey(objectKey)) {
            frequency = objectsStorage.get(objectKey) + 1;
        }
        objectsStorage.put(objectKey, frequency);
    }
}