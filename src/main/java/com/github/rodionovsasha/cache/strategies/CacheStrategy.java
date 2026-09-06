package com.github.rodionovsasha.cache.strategies;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tracks eviction metadata. Callers must synchronize access to a strategy.
 * Equal priorities are resolved by insertion order, independently of key ordering.
 */
public abstract class CacheStrategy<K> {
    private final LinkedHashMap<K, Long> objectsStorage = new LinkedHashMap<>();

    protected CacheStrategy() {
    }

    /** Records an insertion or update, preserving the legacy access-counting semantics. */
    public abstract void putObject(K key);

    public void onInsert(K key) {
        putObject(Objects.requireNonNull(key, "key"));
    }

    /** A cache miss must not create eviction metadata. */
    public void onAccess(K key) {
        if (isObjectPresent(key)) {
            putObject(key);
        }
    }

    protected final Map<K, Long> objectsStorage() {
        return objectsStorage;
    }

    protected final void recordRecency(K key) {
        objectsStorage.putLast(Objects.requireNonNull(key, "key"), 0L);
    }

    protected final Optional<K> selectByRecency(boolean mostRecent) {
        var entry = mostRecent ? objectsStorage.lastEntry() : objectsStorage.firstEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getKey());
    }

    public void removeObject(K key) {
        objectsStorage.remove(key);
    }

    public boolean isObjectPresent(K key) {
        return objectsStorage.containsKey(key);
    }

    /** Selects without removing; an empty strategy has no victim. */
    public Optional<K> selectVictim() {
        Map.Entry<K, Long> victim = null;
        for (var entry : objectsStorage.entrySet()) {
            if (victim == null || entry.getValue() < victim.getValue()) {
                victim = entry;
            }
        }
        return victim == null ? Optional.empty() : Optional.of(victim.getKey());
    }

    /** Compatibility API: throws NoSuchElementException when empty. */
    public K getReplacedKey() {
        return selectVictim().orElseThrow();
    }

    public void clear() {
        objectsStorage.clear();
    }
}
