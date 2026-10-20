package com.crowdsense.dto;

public record JourneyConfirmationResponse(
        boolean awarded,
        int pointsAwarded,
        int newRewardBalance,
        String reason,
        String journeyKey
) {
}
