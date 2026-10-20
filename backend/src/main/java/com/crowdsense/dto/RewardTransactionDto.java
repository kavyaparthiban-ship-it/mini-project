package com.crowdsense.dto;

import java.time.Instant;

public record RewardTransactionDto(
        Long transactionId,
        String journeyKey,
        int points,
        String transactionType,
        String description,
        Instant createdAt
) {
}
