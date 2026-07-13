package com.github.rodionovsasha.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Copyright (©) 2014. Rodionov Aleksandr
 */

public class MemoryCacheTest {
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";

    private MemoryCache<Integer, String> memoryCache;

    @BeforeEach
    public void init() {
        memoryCache = new MemoryCache<>(3);
    }

    @AfterEach
    public void clearCache() {
        memoryCache.clearCache();
    }

    @Test
    public void shouldPutGetAndRemoveObjectTest() {
        memoryCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getFromCache(0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.removeFromCache(0);
        assertNull(memoryCache.getFromCache(0));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        memoryCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getFromCache(0));
        assertNull(memoryCache.getFromCache(111));
    }

    @Test
    public void shouldNotRemoveObjectFromCacheIfNotExistsTest() {
        memoryCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getFromCache(0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.removeFromCache(5);
        assertEquals(VALUE1, memoryCache.getFromCache(0));
    }

    @Test
    public void shouldGetCacheSizeTest() {
        memoryCache.putToCache(0, VALUE1);
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.putToCache(1, VALUE2);
        assertEquals(2, memoryCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(memoryCache.isObjectPresent(0));

        memoryCache.putToCache(0, VALUE1);
        assertTrue(memoryCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        memoryCache = new MemoryCache<>(5);

        IntStream.range(0, 4).forEach(i -> memoryCache.putToCache(i, "String " + i));

        assertTrue(memoryCache.hasEmptyPlace());
        memoryCache.putToCache(5, "String");
        assertFalse(memoryCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        IntStream.range(0, 3).forEach(i -> memoryCache.putToCache(i, "String " + i));

        assertEquals(3, memoryCache.getCacheSize());
        memoryCache.clearCache();
        assertEquals(0, memoryCache.getCacheSize());
    }
}
