package cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (©) 2015. Rodionov Alexander
 */

public class TwoLevelCacheTest {
    private TwoLevelCache<Integer, String> twoLevelCache;

    @Before
    public void init() {
        twoLevelCache = new TwoLevelCache<Integer, String>(1, 1);
    }

    @After
    public void clearCache() {
        twoLevelCache.clearCache();
    }

    @Test
    public void putGetAndRemoveObjectTest() {
        assertTrue(twoLevelCache.getObjectFromCache(0) == null);
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.getObjectFromCache(0).equals("String 1"));
        twoLevelCache.removeObjectFromCache(0);
        assertTrue(twoLevelCache.getObjectFromCache(0) == null);
    }

    @Test
    public void putObjectIntoCacheWhenFirstLevelHasEmptyPlaceTest() {
        assertTrue(twoLevelCache.getObjectFromCache(0) == null);
        assertTrue(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.getObjectFromCache(0).equals("String 1"));
        assertTrue(twoLevelCache.getFirstLevelCache().getObjectFromCache(0).equals("String 1"));
    }

    @Test
    public void putObjectIntoCacheWhenObjectPresentsInFirstLevelCacheTest() {
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.getObjectFromCache(0).equals("String 1"));
        assertTrue(twoLevelCache.getFirstLevelCache().getObjectFromCache(0).equals("String 1"));
        assertTrue(twoLevelCache.getFirstLevelCache().getCacheSize() == 1);
        // put the same key with other value
        twoLevelCache.putObjectIntoCache(0, "String 2");
        assertTrue(twoLevelCache.getObjectFromCache(0).equals("String 2"));
        assertTrue(twoLevelCache.getFirstLevelCache().getObjectFromCache(0).equals("String 2"));
        assertTrue(twoLevelCache.getFirstLevelCache().getCacheSize() == 1);
    }

    @Test
    public void putObjectIntoCacheWhenSecondLevelHasEmptyPlaceTest() {
        for (int i = 0; i < 1; i++) {
            twoLevelCache.putObjectIntoCache(i, "String " + i);
        }
        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        assertTrue(twoLevelCache.getSecondLevelCache().hasEmptyPlace());
        twoLevelCache.putObjectIntoCache(2, "String 2");
        assertTrue(twoLevelCache.getObjectFromCache(2).equals("String 2"));
        assertTrue(twoLevelCache.getSecondLevelCache().getObjectFromCache(2).equals("String 2"));
    }

    @Test
    public void putObjectIntoCacheWhenObjectPresentsInSecondLevelPlaceTest() {
        for (int i = 0; i < 1; i++) {
            twoLevelCache.putObjectIntoCache(i, "String " + i);
        }
        assertFalse(twoLevelCache.getFirstLevelCache().hasEmptyPlace());
        twoLevelCache.putObjectIntoCache(2, "String 2");
        assertTrue(twoLevelCache.getObjectFromCache(2).equals("String 2"));
        assertTrue(twoLevelCache.getSecondLevelCache().getObjectFromCache(2).equals("String 2"));
        assertTrue(twoLevelCache.getSecondLevelCache().getCacheSize() == 1);
        // put the same key with other value
        twoLevelCache.putObjectIntoCache(2, "String 3");
        assertTrue(twoLevelCache.getObjectFromCache(2).equals("String 3"));
        assertTrue(twoLevelCache.getSecondLevelCache().getObjectFromCache(2).equals("String 3"));
        assertTrue(twoLevelCache.getSecondLevelCache().getCacheSize() == 1);
    }

    @Test
    public void putObjectIntoCacheWhenObjectShouldBeReplacedTest() {
        for (int i = 0; i < 2; i++) {
            twoLevelCache.putObjectIntoCache(i, "String " + i);
        }
        assertFalse(twoLevelCache.hasEmptyPlace());
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(3));
        twoLevelCache.putObjectIntoCache(3, "String 3");
        assertTrue(twoLevelCache.getObjectFromCache(3).equals("String 3"));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(3));
    }

    @Test
    public void getCacheSizeTest() {
        assertTrue(twoLevelCache.getCacheSize() == 0);
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.getCacheSize() == 1);
        twoLevelCache.putObjectIntoCache(1, "String 2");
        assertTrue(twoLevelCache.getCacheSize() == 2);
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        assertFalse(twoLevelCache.isObjectPresent(0));
        twoLevelCache.putObjectIntoCache(0, "String 1");
        assertTrue(twoLevelCache.hasEmptyPlace());
        twoLevelCache.putObjectIntoCache(1, "String 2");
        assertFalse(twoLevelCache.hasEmptyPlace());
    }

    @Test
    public void clearCacheTest() {
        assertTrue(twoLevelCache.getCacheSize() == 0);
        twoLevelCache.putObjectIntoCache(0, "String 1");
        twoLevelCache.putObjectIntoCache(1, "String 2");
        assertTrue(twoLevelCache.getCacheSize() == 2);
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(0));
        assertTrue(twoLevelCache.getStrategy().isObjectPresent(1));
        twoLevelCache.clearCache();
        assertTrue(twoLevelCache.getCacheSize() == 0);
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(0));
        assertFalse(twoLevelCache.getStrategy().isObjectPresent(1));
    }
}