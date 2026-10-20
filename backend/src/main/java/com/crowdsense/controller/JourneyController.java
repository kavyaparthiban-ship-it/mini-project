package com.crowdsense.controller;

import com.crowdsense.dto.JourneyConfirmationRequest;
import com.crowdsense.dto.JourneyConfirmationResponse;
import com.crowdsense.service.RewardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passengers/{passengerId}/journeys")
public class JourneyController {

    private final RewardService rewardService;

    public JourneyController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping("/confirm")
    public JourneyConfirmationResponse confirm(@PathVariable Long passengerId,
                                               @Valid @RequestBody JourneyConfirmationRequest request) {
        return rewardService.confirmJourney(
                passengerId,
                request.selectedBusId(),
                request.source(),
                request.destination(),
                request.boardStopId(),
                request.journeyKey()
        );
    }
}
