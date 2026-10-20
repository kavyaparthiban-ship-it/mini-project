package com.crowdsense.service;

import com.crowdsense.config.CrowdSenseProperties;
import com.crowdsense.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationEngineServiceTest {

    private RecommendationEngineService engine = new RecommendationEngineService(new CrowdSenseProperties());

    @Test
    void ranksLessCrowdedHigher() {
        Bus crowded = new Bus(1L, "101", 50, "R101", "Route 101");
        Bus better = new Bus(2L, "102", 50, "R102", "Route 102");
        Occupancy occCrowded = Occupancy.fromPercent(94.0, 50, false);
        Occupancy occBetter = Occupancy.fromPercent(42.0, 50, false);

        List<RecommendationEngineService.Candidate> candidates = List.of(
                new RecommendationEngineService.Candidate(crowded, occCrowded, 1L, "Source", 4L, 0.2, 10, 15, 3.0),
                new RecommendationEngineService.Candidate(better, occBetter, 1L, "Source", 4L, 0.2, 6, 15, 3.0)
        );

        List<Recommendation> ranked = engine.rank(candidates, PassengerPreference.DEFAULT);
        assertEquals("102", ranked.get(0).getBus().getNumber());
        assertTrue(ranked.get(0).isRecommended());
        assertFalse(ranked.get(1).isRecommended());
    }

    @Test
    void missingOccupancyIsNotBoosted() {
        Bus good = new Bus(1L, "101", 50, "R101", "Route 101");
        Bus unknown = new Bus(2L, "102", 50, "R102", "Route 102");
        Occupancy known = Occupancy.fromPercent(30.0, 50, false);
        Occupancy missing = Occupancy.fromPercent(null, 50, true);

        List<RecommendationEngineService.Candidate> candidates = List.of(
                new RecommendationEngineService.Candidate(unknown, missing, 1L, "Source", 4L, 0.2, 5, 10, 2.0),
                new RecommendationEngineService.Candidate(good, known, 1L, "Source", 4L, 0.2, 5, 10, 2.0)
        );

        List<Recommendation> ranked = engine.rank(candidates, PassengerPreference.DEFAULT);
        assertEquals("101", ranked.get(0).getBus().getNumber());
    }

    @Test
    void tieBrokenDeterministically() {
        Bus a = new Bus(1L, "105", 50, "R105", "Route 105");
        Bus b = new Bus(2L, "102", 50, "R102", "Route 102");
        Occupancy oa = Occupancy.fromPercent(40.0, 50, false);
        Occupancy ob = Occupancy.fromPercent(40.0, 50, false);

        List<RecommendationEngineService.Candidate> candidates = List.of(
                new RecommendationEngineService.Candidate(a, oa, 1L, "Source", 4L, 0.1, 6, 10, 2.0),
                new RecommendationEngineService.Candidate(b, ob, 1L, "Source", 4L, 0.1, 6, 10, 2.0)
        );

        List<Recommendation> ranked = engine.rank(candidates, PassengerPreference.DEFAULT);
        // Tie on everything -> bus number order
        assertEquals("102", ranked.get(0).getBus().getNumber());
    }
}
