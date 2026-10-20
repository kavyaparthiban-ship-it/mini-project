package com.crowdsense.dto;

import java.util.List;

public record RouteDto(
        Long routeId,
        String routeCode,
        String routeName,
        String transportMode,
        List<RouteStopDto> stops
) {
}
