package com.github.rodionovsasha.cache.strategies;

/*
 * Copyright (©) 2017. Rodionov Aleksandr
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrategyTypeTest {
    @Test
    public void shouldGetCorrectValuesFromEnumTest() {
        assertEquals(StrategyType.LFU, StrategyType.valueOf("LFU"));
        assertEquals(StrategyType.LRU, StrategyType.valueOf("LRU"));
        assertEquals(StrategyType.MRU, StrategyType.valueOf("MRU"));
    }

    @Test
    public void shouldThrowExceptionWhenTypeIsNotCorrectTest() {
        assertThrows(IllegalArgumentException.class, () -> StrategyType.valueOf("wrong_value"));
    }
}
