package com.github.rodionovsasha.cache.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Copyright (©) 2017. Rodionov Aleksandr
 */

public class ComparatorImplTest {
    private ComparatorImpl<String> comparator;
    private Map<String, Long> comparatorMap;

    @BeforeEach
    public void setUp() {
        comparatorMap = new HashMap<>();
        comparator = new ComparatorImpl<>(comparatorMap);
    }

    @Test
    public void differentKeysWithEqualValuesShouldNotBeEquals() {
        //Given
        comparatorMap.put("key1", 1L);
        comparatorMap.put("key2", 1L);
        //When
        int result = comparator.compare("key1", "key2");
        //Then
        assertNotEquals(0, result);
    }

    @Test
    public void key1ShouldBeLaterThanKey2() {
        //Given
        comparatorMap.put("key1", 2L);
        comparatorMap.put("key2", 1L);
        //When
        int result = comparator.compare("key1", "key2");
        //Then
        assertTrue(result > 0);
    }

    @Test
    public void key1ShouldBeEarlierThanKey2() {
        //Given
        comparatorMap.put("key1", 1L);
        comparatorMap.put("key2", 2L);
        //When
        int result = comparator.compare("key1", "key2");
        //Then
        assertTrue(result < 0);
    }

    @Test
    public void shouldInitNPE() {
        //Given
        comparatorMap.put("key1", 1L);
        comparatorMap.put("key2", null);
        //When / Then
        assertThrows(NullPointerException.class, () -> comparator.compare("key1", "key2"));
    }
}
