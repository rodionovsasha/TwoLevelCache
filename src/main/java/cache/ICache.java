package cache;

public interface ICache<KeyType, ValueType> {
    void putObjectIntoCache(KeyType key, ValueType val);
    ValueType getObjectFromCache(KeyType key);
    void removeObjectFromCache(KeyType key);
    int getCacheSize();
    boolean isObjectPresent(KeyType key);
    boolean hasEmptyPlace();
    void clearCache();
}