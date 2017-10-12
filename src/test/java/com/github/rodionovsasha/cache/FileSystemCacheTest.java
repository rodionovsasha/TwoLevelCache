package com.github.rodionovsasha.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

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
        fileSystemCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getFromCache(0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.removeFromCache(0);
        assertNull(fileSystemCache.getFromCache(0));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        fileSystemCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getFromCache(0));
        assertNull(fileSystemCache.getFromCache(111));
    }

    @Test
    public void shouldNotRemoveObjectFromCacheIfNotExistsTest() {
        fileSystemCache.putToCache(0, VALUE1);
        assertEquals(VALUE1, fileSystemCache.getFromCache(0));
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.removeFromCache(5);
        assertEquals(VALUE1, fileSystemCache.getFromCache(0));
    }

    @Test
    public void shouldGetCacheSizeTest() {
        fileSystemCache.putToCache(0, VALUE1);
        assertEquals(1, fileSystemCache.getCacheSize());

        fileSystemCache.putToCache(1, VALUE2);
        assertEquals(2, fileSystemCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(fileSystemCache.isObjectPresent(0));

        fileSystemCache.putToCache(0, VALUE1);
        assertTrue(fileSystemCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        fileSystemCache = new FileSystemCache<>(5);

        IntStream.range(0, 4).forEach(i -> fileSystemCache.putToCache(i, "String " + i));
        assertTrue(fileSystemCache.hasEmptyPlace());
        fileSystemCache.putToCache(5, "String");
        assertFalse(fileSystemCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        IntStream.range(0, 3).forEach(i -> fileSystemCache.putToCache(i, "String " + i));

        assertEquals(3, fileSystemCache.getCacheSize());
        fileSystemCache.clearCache();
        assertEquals(0, fileSystemCache.getCacheSize());
    }
}
