package com.crowdsense.controller;

import com.crowdsense.dto.BusDto;
import com.crowdsense.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final RecommendationService recommendationService;

    public BusController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<BusDto> getAll() {
        return recommendationService.getAllBuses();
    }
}
