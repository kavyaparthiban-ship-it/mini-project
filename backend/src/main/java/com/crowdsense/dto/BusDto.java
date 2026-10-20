package com.crowdsense.dto;

import java.util.List;

public record BusDto(
        Long busId,
        String busNumber,
        String routeCode,
        String routeName,
        String transportMode,
        Integer capacity,
        Long currentStopId,
        String currentStopName,
        Double occupancyPercent,
        String crowdCategory,
        String recordedAt,
        Boolean occupancyStale,
        Boolean occupancyMissing
) {
}
