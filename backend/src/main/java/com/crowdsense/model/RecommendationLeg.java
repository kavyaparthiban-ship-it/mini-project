package com.crowdsense.model;

public record RecommendationLeg(
        Long vehicleId,
        String vehicleNumber,
        String routeCode,
        String routeName,
        String transportMode,
        Long fromStopId,
        String fromStopName,
        Long toStopId,
        String toStopName,
        int estimatedWaitMinutes,
        int estimatedTravelMinutes,
        Double occupancyPercent,
        String crowdCategory,
        boolean occupancyStale,
        boolean occupancyMissing
) {
}
