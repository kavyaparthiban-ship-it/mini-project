package com.crowdsense.dto;

public record RouteStopDto(
        Long stopId,
        String stopName,
        String stopCode,
        Integer stopSequence,
        Double distanceFromStartKm
) {
}
