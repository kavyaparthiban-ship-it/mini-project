package com.crowdsense.dto;

import java.util.List;

public record RecommendationResponse(
        String source,
        String destination,
        String sourceStopName,
        String destinationStopName,
        List<RecommendationItemDto> availableBuses,
        RecommendationItemDto recommended,
        String explanation,
        String demoNote,
        ScoringInfo scoring
) {
    public record ScoringInfo(
            double weightCrowd,
            double weightWait,
            double weightTravel,
            double weightWalk,
            String formula
    ) {
    }
}
