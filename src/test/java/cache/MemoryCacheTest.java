package cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MemoryCacheTest {
    private MemoryCache<Integer, String> memoryCache;

    @Before
    public void init() {
        memoryCache = new MemoryCache<Integer, String>();
    }

    @After
    public void clearCache() {
        memoryCache.clearCache();
    }

    @Test
    public void putGetAndRemoveObjectTest() {
        assertTrue(memoryCache.getObjectFromCache(0) == null);
        memoryCache.putObjectIntoCache(0, "String 1");
        assertTrue(memoryCache.getObjectFromCache(0).equals("String 1"));
        memoryCache.removeObjectFromCache(0);
        assertTrue(memoryCache.getObjectFromCache(0) == null);
    }

    @Test
    public void getCacheSizeTest() {
        assertTrue(memoryCache.getCacheSize() == 0);
        memoryCache.putObjectIntoCache(0, "String 1");
        assertTrue(memoryCache.getCacheSize() == 1);
        memoryCache.putObjectIntoCache(1, "String 2");
        assertTrue(memoryCache.getCacheSize() == 2);
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(memoryCache.isObjectPresent(0));
        memoryCache.putObjectIntoCache(0, "String 1");
        assertTrue(memoryCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        memoryCache = new MemoryCache<Integer, String>(5);
        for (int i = 0; i < 4; i++) {
            memoryCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(memoryCache.hasEmptyPlace());
        memoryCache.putObjectIntoCache(5, "String");
        assertFalse(memoryCache.hasEmptyPlace());
    }

    @Test
    public void clearCacheTest() {
        assertTrue(memoryCache.getCacheSize() == 0);
        for (int i = 0; i < 3; i++) {
            memoryCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(memoryCache.getCacheSize() == 3);
        memoryCache.clearCache();
        assertTrue(memoryCache.getCacheSize() == 0);
    }
}