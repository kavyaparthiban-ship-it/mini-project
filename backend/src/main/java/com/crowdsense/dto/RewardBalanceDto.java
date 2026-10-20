package com.crowdsense.dto;

import java.util.List;

public record RewardBalanceDto(
        Long passengerId,
        String passengerName,
        int rewardBalance
) {
}
