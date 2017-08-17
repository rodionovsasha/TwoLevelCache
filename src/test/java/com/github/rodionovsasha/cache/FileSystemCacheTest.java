package com.github.rodionovsasha.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

public class FileSystemCacheTest {
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";

    private FileSystemCache<Integer, String> fileSystemCache;

    @Before
    public void init() {
        fileSystemCache = new FileSystemCache<>();
    }

    @After
    public void clearCache() {
        fileSystemCache.clearCache();
    }

    @Test
    public void shouldPutGetAndRemoveObjectTest() {
        fileSystemCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getObjectFromCache(0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.removeObjectFromCache(0);
        assertNull(fileSystemCache.getObjectFromCache(0));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        fileSystemCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getObjectFromCache(0));
        assertNull(fileSystemCache.getObjectFromCache(111));
    }

    @Test
    public void shouldNotRemoveObjectFromCacheIfNotExistsTest() {
        fileSystemCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getObjectFromCache(0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.removeObjectFromCache(5);
        assertEquals(VALUE1, fileSystemCache.getObjectFromCache(0));
    }

    @Test
    public void shouldGetCacheSizeTest() {
        fileSystemCache.putObjectIntoCache(0, VALUE1);
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.putObjectIntoCache(1, VALUE2);
        assertEquals(2, fileSystemCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(fileSystemCache.isObjectPresent(0));

        fileSystemCache.putObjectIntoCache(0, VALUE1);
        assertTrue(fileSystemCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        fileSystemCache = new FileSystemCache<>(5);

        for (int i = 0; i < 4; i++) {
            fileSystemCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(fileSystemCache.hasEmptyPlace());
        fileSystemCache.putObjectIntoCache(5, "String");
        assertFalse(fileSystemCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        for (int i = 0; i < 3; i++) {
            fileSystemCache.putObjectIntoCache(i, "String " + i);
        }
        assertEquals(3, fileSystemCache.getCacheSize());
        fileSystemCache.clearCache();
        assertEquals(0, fileSystemCache.getCacheSize());
    }
}
