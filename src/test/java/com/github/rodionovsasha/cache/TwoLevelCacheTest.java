package com.github.rodionovsasha.cache;

import com.github.rodionovsasha.cache.strategies.StrategyType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

/*
 * Copyright (©) 2014. Rodionov Alexander
 */

public class TwoLevelCacheTest {
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";

    private TwoLevelCache<Integer, String> twoLevelCache;

    @Before
    public void init() {
        twoLevelCache = new TwoLevelCache<>(1, 1);
    }

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void shouldPutGetAndRemoveObjectTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.removeObjectFromCache(0);
        assertNull(twoLevelCache.getObjectFromCache(0));
    }

    @Test
    public void shouldRemoveObjectFromFirstLevelTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        twoLevelCache.putObjectIntoCache(1, VALUE2);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getObjectFromCache(1));

        twoLevelCache.removeObjectFromCache(0);

        assertNull(twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getObjectFromCache(1));
    }

    @Test
    public void shouldRemoveObjectFromSecondLevelTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        twoLevelCache.putObjectIntoCache(1, VALUE2);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getObjectFromCache(1));

        twoLevelCache.removeObjectFromCache(1);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertNull(twoLevelCache.getSecondLevelCache().getObjectFromCache(1));
    }

    @Test
    public void shouldNotGetObjectFromCacheIfNotExistsTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertNull(twoLevelCache.getObjectFromCache(111));
    }

    @Test
    public void shouldRemoveDuplicatedObjectFromSecondLevelWhenFirstLevelHasEmptyPlaceTest() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasEmptyPlace());

        twoLevelCache.getSecondLevelCache().putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getSecondLevelCache().getObjectFromCache(0));

        twoLevelCache.putObjectIntoCache(0, VALUE1);

        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenFirstLevelHasEmptyPlaceTest() {
        assertTrue(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectExistsInFirstLevelCacheTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());

        // put the same key with other value
        twoLevelCache.putObjectIntoCache(0, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getObjectFromCache(0));
        assertEquals(VALUE2, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertEquals(1, twoLevelCache.getFirstLevelCache().getCacheSize());
    }

    @Test
    public void shouldPutObjectIntoCacheWhenSecondLevelHasEmptyPlaceTest() {
        IntStream.range(0, 1).forEach(i -> twoLevelCache.putObjectIntoCache(i, "String " + i));

        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        assertTrue(twoLevelCache.getSecondLevelCache().hasEmptyPlace());

        twoLevelCache.putObjectIntoCache(2, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getObjectFromCache(2));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getObjectFromCache(2));
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectExistsInSecondLevelTest() {
        IntStream.range(0, 1).forEach(i -> twoLevelCache.putObjectIntoCache(i, "String " + i));

        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());

        twoLevelCache.putObjectIntoCache(2, VALUE2);

        assertEquals(VALUE2, twoLevelCache.getObjectFromCache(2));
        assertEquals(VALUE2, twoLevelCache.getSecondLevelCache().getObjectFromCache(2));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());

        // put the same key with other value
        twoLevelCache.putObjectIntoCache(2, VALUE3);

        assertEquals(VALUE3, twoLevelCache.getObjectFromCache(2));
        assertEquals(VALUE3, twoLevelCache.getSecondLevelCache().getObjectFromCache(2));
        assertEquals(1, twoLevelCache.getSecondLevelCache().getCacheSize());
    }

    @Test
    public void shouldPutObjectIntoCacheWhenObjectShouldBeReplacedTest() {
        IntStream.range(0, 2).forEach(i -> twoLevelCache.putObjectIntoCache(i, "String " + i));

        assertFalse(twoLevelCache.hasEmptyPlace());
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(3));

        twoLevelCache.putObjectIntoCache(3, VALUE3);

        assertTrue(twoLevelCache.getObjectFromCache(3).equals(VALUE3));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(3));
        assertTrue(twoLevelCache.getFirstLevelCache().isObjectPresent(3));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(3));
    }

    @Test
    public void shouldGetCacheSizeTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(1, twoLevelCache.getCacheSize());

        twoLevelCache.putObjectIntoCache(1, VALUE2);
        assertEquals(2, twoLevelCache.getCacheSize());
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));

        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertTrue(twoLevelCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertTrue(twoLevelCache.hasEmptyPlace());

        twoLevelCache.putObjectIntoCache(1, VALUE2);
        assertFalse(twoLevelCache.hasEmptyPlace());
    }

    @Test
    public void shouldClearCacheTest() {
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        twoLevelCache.putObjectIntoCache(1, VALUE2);

        assertEquals(2, twoLevelCache.getCacheSize());
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(0));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(1));

        twoLevelCache.clearCache();

        assertEquals(0, twoLevelCache.getCacheSize());
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(0));
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(1));
    }

    @Test
    public void shouldUseLRUStrategyTest() {
        twoLevelCache = new TwoLevelCache<>(1, 1, StrategyType.LRU);
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }

    @Test
    public void shouldUseMRUStrategyTest() {
        twoLevelCache = new TwoLevelCache<>(1, 1, StrategyType.MRU);
        twoLevelCache.putObjectIntoCache(0, VALUE1);
        assertEquals(VALUE1, twoLevelCache.getObjectFromCache(0));
        assertEquals(VALUE1, twoLevelCache.getFirstLevelCache().getObjectFromCache(0));
        assertFalse(twoLevelCache.getSecondLevelCache().isObjectPresent(0));
    }
}