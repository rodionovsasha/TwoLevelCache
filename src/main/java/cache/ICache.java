package cache;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

interface ICache<KeyType, ValueType> {
    void putObjectIntoCache(KeyType objectKey, ValueType objectValue);
    ValueType getObjectFromCache(KeyType objectKey);
    void removeObjectFromCache(KeyType objectKey);
    int getCacheSize();
    boolean isObjectPresent(KeyType objectKey);
    boolean hasEmptyPlace();
    void clearCache();
}