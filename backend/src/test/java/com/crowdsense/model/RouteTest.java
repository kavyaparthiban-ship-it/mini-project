package com.crowdsense.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteTest {

    @Test
    void calculatesTripDistanceAndSegmentInReverseDirection() {
        Route route = new Route(1L, "R101", "Central Terminal - University Circle", "BUS", List.of(
                new Stop(2L, "Market Junction", "MKJ", 2, 0.9),
                new Stop(3L, "City Hospital", "CTH", 3, 1.9)
        ));

        assertEquals(1.0, route.distanceBetween(3L, 2L), 0.0001);
        assertEquals(
                List.of("City Hospital", "Market Junction"),
                route.segment(3L, 2L).stream().map(Stop::getName).toList()
        );
    }
}
