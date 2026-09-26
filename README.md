# TwoLevelCache

`TwoLevelCache` caches serializable values in two independent levels:

- L1 is an in-memory cache for hot values.
- L2 is a filesystem-backed cache that stores serialized values.

Writes go to every enabled level. A value found in L2 is promoted to L1. When L1 evicts a value, its L2 copy remains available. Each level has its own entry-count limit and eviction strategy (`LFU`, `LRU`, or `MRU`). Keys do not need to implement `Serializable`.

## Usage

```java
import com.github.rodionovsasha.cache.TwoLevelCache;
import com.github.rodionovsasha.cache.TwoLevelCacheConfig;
import com.github.rodionovsasha.cache.strategies.StrategyType;

try (var cache = new TwoLevelCache<String, String>(
        new TwoLevelCacheConfig(100, 10_000, StrategyType.LRU, StrategyType.LFU))) {
    cache.put("user:42", "cached value");
    String value = cache.get("user:42");
    cache.remove("user:42");
}
```

The compatibility constructors configure the same strategy for both levels:

```java
var cache = new TwoLevelCache<String, String>(100, 10_000, StrategyType.LRU);
```

`memoryCapacity` and `fileCapacity` are numbers of entries, not bytes. A zero capacity disables that level; both capacities cannot be zero. `getCacheSize()` reports logical keys, so an object present in both levels is counted once.

## Disk storage and failures

Values must implement `Serializable`, because L2 uses Java object serialization. L2 creates a private temporary subdirectory by default. To place that directory under a chosen root, pass `fileCacheRoot` in `TwoLevelCacheConfig`; the cache still creates an isolated child directory to avoid deleting unrelated files.

Call `close()` (or use try-with-resources) to remove the cache directory and its files. If writing to L2 fails, the write is not added to L2's eviction metadata; a successful L1 write can still serve the value until it is evicted.
