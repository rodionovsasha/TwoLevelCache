package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.StrategyType;

import java.nio.file.Path;
import java.util.Objects;

/** Configuration for a two-level cache. Capacities are entry counts. */
public record TwoLevelCacheConfig(
        int memoryCapacity,
        int fileCapacity,
        StrategyType memoryStrategy,
        StrategyType fileStrategy,
        Path fileCacheRoot) {
    public TwoLevelCacheConfig {
        if (memoryCapacity < 0 || fileCapacity < 0) {
            throw new IllegalArgumentException("Cache capacities must not be negative");
        }
        if ((long) memoryCapacity + fileCapacity == 0) {
            throw new IllegalArgumentException("Total cache capacity must be greater than zero");
        }
        Objects.requireNonNull(memoryStrategy, "memoryStrategy");
        Objects.requireNonNull(fileStrategy, "fileStrategy");
    }

    public TwoLevelCacheConfig(int memoryCapacity, int fileCapacity,
                               StrategyType memoryStrategy, StrategyType fileStrategy) {
        this(memoryCapacity, fileCapacity, memoryStrategy, fileStrategy, null);
    }
}
