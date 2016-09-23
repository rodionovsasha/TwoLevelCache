package cache.strategies;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

/**
 * MRU (англ. Most Recently Used - Наиболее недавно использовавшийся) — вытесняется последний использованный буфер; в отличе от LRU, в первую очередь вытесняется последний использованный элемент.
 * Когда файл периодически сканируется по циклической схеме, MRU — наилучший алгоритм вытеснения.
 * Что для схем случайного доступа и циклического сканирования больших наборов данных (иногда называемых схемами циклического доступа)
 * алгоритмы кэширования MRU имеют больше попаданий по сравнению с LRU за счет их стремления к сохранению старых данных.
 * Алгоритмы MRU наиболее полезны в случаях, когда чем старше элемент, тем больше обращений к нему происходит.
 */

public class MRUStrategy<KeyType> extends CacheStrategy<KeyType> {
    @Override
    public void putObject(KeyType objectKey) {
        objectsStorage.put(objectKey, System.nanoTime());
    }

    @Override
    public KeyType getUsedKey() {
        sortedObjectsStorage.putAll(objectsStorage);
        return sortedObjectsStorage.lastKey();
    }
}