package com.crowdsense.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OccupancyTest {

    @Test
    void fromPercent() {
        Occupancy o = Occupancy.fromPercent(94.0, 50, false);
        assertEquals(47, o.getPassengerCount());
        assertEquals(94.0, o.getOccupancyPercent());
        assertEquals(CrowdCategory.CRITICAL, o.getCrowdCategory());
        assertFalse(o.isMissing());
        assertFalse(o.isStale());
    }

    @Test
    void missingWhenNull() {
        Occupancy o = Occupancy.fromPercent(null, 50, true);
        assertTrue(o.isMissing());
        assertNull(o.getOccupancyPercent());
        assertEquals(CrowdCategory.UNKNOWN, o.getCrowdCategory());
    }

    @Test
    void fromCount() {
        Occupancy o = Occupancy.fromCount(21, 50, false);
        assertEquals(42.0, o.getOccupancyPercent());
    }
}
