package cache;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

interface Cache<K, V> {
    void putObjectIntoCache(K key, V value);
    V getObjectFromCache(K key);
    void removeObjectFromCache(K key);
    int getCacheSize();
    boolean isObjectPresent(K key);
    boolean hasEmptyPlace();
    void clearCache();
}
