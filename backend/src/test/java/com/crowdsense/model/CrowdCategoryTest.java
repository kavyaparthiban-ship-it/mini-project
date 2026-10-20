package com.crowdsense.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrowdCategoryTest {

    @Test
    void classifyDefaults() {
        assertEquals(CrowdCategory.LOW, CrowdCategory.classify(10.0));
        assertEquals(CrowdCategory.MODERATE, CrowdCategory.classify(40.0));
        assertEquals(CrowdCategory.HIGH, CrowdCategory.classify(65.0));
        assertEquals(CrowdCategory.CRITICAL, CrowdCategory.classify(85.0));
        assertEquals(CrowdCategory.CRITICAL, CrowdCategory.classify(94.0));
    }

    @Test
    void classifyNullReturnsUnknown() {
        assertEquals(CrowdCategory.UNKNOWN, CrowdCategory.classify(null));
    }

    @Test
    void isOvercrowded() {
        assertFalse(CrowdCategory.LOW.isOvercrowded());
        assertFalse(CrowdCategory.MODERATE.isOvercrowded());
        assertTrue(CrowdCategory.HIGH.isOvercrowded());
        assertTrue(CrowdCategory.CRITICAL.isOvercrowded());
    }
}
