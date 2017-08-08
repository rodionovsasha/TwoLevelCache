package cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

public class MemoryCacheTest {
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";

    private MemoryCache<Integer, String> memoryCache;

    @Before
    public void init() {
        memoryCache = new MemoryCache<>(3);
    }

    @After
    public void clearCache() {
        memoryCache.clearCache();
    }

    @Test
    public void shouldPutGetAndRemoveObjectTest() {
        memoryCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getObjectFromCache(0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.removeObjectFromCache(0);
        assertNull(memoryCache.getObjectFromCache(0));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        memoryCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getObjectFromCache(0));
        assertNull(memoryCache.getObjectFromCache(111));
    }

    @Test
    public void shouldNotRemoveObjectFromCacheIfNotExistsTest() {
        memoryCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, memoryCache.getObjectFromCache(0));
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.removeObjectFromCache(5);
        assertEquals(VALUE1, memoryCache.getObjectFromCache(0));
    }

    @Test
    public void shouldGetCacheSizeTest() {
        memoryCache.putObjectIntoCache(0, VALUE1);
        assertEquals(1, memoryCache.getCacheSize());

        memoryCache.putObjectIntoCache(1, VALUE2);
        assertEquals(2, memoryCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(memoryCache.isObjectPresent(0));

        memoryCache.putObjectIntoCache(0, VALUE1);
        assertTrue(memoryCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        memoryCache = new MemoryCache<>(5);

        for (int i = 0; i < 4; i++) {
            memoryCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(memoryCache.hasEmptyPlace());
        memoryCache.putObjectIntoCache(5, "String");
        assertFalse(memoryCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        for (int i = 0; i < 3; i++) {
            memoryCache.putObjectIntoCache(i, "String " + i);
        }
        assertEquals(3, memoryCache.getCacheSize());
        memoryCache.clearCache();
        assertEquals(0, memoryCache.getCacheSize());
    }
}