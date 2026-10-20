package com.crowdsense.controller;

import com.crowdsense.dto.RewardBalanceDto;
import com.crowdsense.dto.RewardTransactionDto;
import com.crowdsense.service.RewardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/passengers/{passengerId}/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    public RewardBalanceDto balance(@PathVariable Long passengerId) {
        return rewardService.getRewardBalance(passengerId);
    }

    @GetMapping("/history")
    public List<RewardTransactionDto> history(@PathVariable Long passengerId) {
        return rewardService.getRewardHistory(passengerId);
    }
}
