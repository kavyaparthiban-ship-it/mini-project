package com.crowdsense.dto;

public record RecommendationLegDto(
        Long vehicleId,
        String vehicleNumber,
        String routeCode,
        String routeName,
        String transportMode,
        Long fromStopId,
        String fromStopName,
        Long toStopId,
        String toStopName,
        Integer estimatedWaitMinutes,
        Integer estimatedTravelMinutes,
        Double occupancyPercent,
        String crowdCategory,
        Boolean occupancyStale,
        Boolean occupancyMissing
) {
}
