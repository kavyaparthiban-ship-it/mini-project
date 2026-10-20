package com.crowdsense.dto;

import java.util.List;

public record RecommendationItemDto(
        Long busId,
        String busNumber,
        String routeCode,
        String routeName,
        String transportMode,
        Double occupancyPercent,
        String crowdCategory,
        Boolean occupancyStale,
        Boolean occupancyMissing,
        Long boardStopId,
        String boardStopName,
        Double walkDistanceKm,
        Integer estimatedWaitMinutes,
        Integer estimatedTravelMinutes,
        Double tripDistanceKm,
        Double score,
        Boolean recommended,
        Double crowdScore,
        Double waitScore,
        Double travelScore,
        Double walkScore,
        List<String> reasons
) {
}
