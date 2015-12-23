package cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileSystemCacheTest {
    private FileSystemCache<Integer, String> fileSystemCache;

    @Before
    public void init() {
        fileSystemCache = new FileSystemCache<Integer, String>();
    }

    @After
    public void clearCache() {
        fileSystemCache.clearCache();
    }

    @Test
    public void putGetAndRemoveObjectTest() {
        assertTrue(fileSystemCache.getObjectFromCache(0) == null);
        fileSystemCache.putObjectIntoCache(0, "String 1");
        assertTrue(fileSystemCache.getObjectFromCache(0).equals("String 1"));
        fileSystemCache.removeObjectFromCache(0);
        assertTrue(fileSystemCache.getObjectFromCache(0) == null);
    }

    @Test
    public void getCacheSizeTest() {
        assertTrue(fileSystemCache.getCacheSize() == 0);
        fileSystemCache.putObjectIntoCache(0, "String 1");
        assertTrue(fileSystemCache.getCacheSize() == 1);
        fileSystemCache.putObjectIntoCache(1, "String 2");
        assertTrue(fileSystemCache.getCacheSize() == 2);
    }

    @Test
    public void isObjectPresentTest() {
        assertFalse(fileSystemCache.isObjectPresent(0));
        fileSystemCache.putObjectIntoCache(0, "String 1");
        assertTrue(fileSystemCache.isObjectPresent(0));
    }

    @Test
    public void isEmptyPlaceTest() {
        fileSystemCache = new FileSystemCache<Integer, String>(5);
        for (int i = 0; i < 4; i++) {
            fileSystemCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(fileSystemCache.hasEmptyPlace());
        fileSystemCache.putObjectIntoCache(5, "String");
        assertFalse(fileSystemCache.hasEmptyPlace());
    }

    @Test
    public void clearCacheTest() {
        assertTrue(fileSystemCache.getCacheSize() == 0);
        for (int i = 0; i < 3; i++) {
            fileSystemCache.putObjectIntoCache(i, "String " + i);
        }
        assertTrue(fileSystemCache.getCacheSize() == 3);
        fileSystemCache.clearCache();
        assertTrue(fileSystemCache.getCacheSize() == 0);
    }
}
