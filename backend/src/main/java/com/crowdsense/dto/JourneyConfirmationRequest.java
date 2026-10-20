package com.crowdsense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JourneyConfirmationRequest(
        @NotNull(message = "passengerId is required")
        Long passengerId,

        @NotNull(message = "selectedBusId is required")
        Long selectedBusId,

        @NotBlank(message = "source is required")
        String source,

        @NotBlank(message = "destination is required")
        String destination,

        Long boardStopId,

        @NotBlank(message = "journeyKey is required")
        String journeyKey
) {
}
