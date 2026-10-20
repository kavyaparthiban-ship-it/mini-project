package com.crowdsense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecommendationRequest(
        @NotBlank(message = "source is required")
        String source,

        @NotBlank(message = "destination is required")
        String destination,

        Long passengerId,

        PreferenceOverride preference
) {
    public record PreferenceOverride(
            Integer preferLowCrowd,
            Integer preferShortWait,
            Integer preferShortTravel,
            Integer preferShortWalk,
            Double maxWalkDistanceKm
    ) {
    }
}
