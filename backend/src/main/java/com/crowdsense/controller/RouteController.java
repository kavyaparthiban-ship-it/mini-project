package com.crowdsense.controller;

import com.crowdsense.dto.RouteDto;
import com.crowdsense.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RecommendationService recommendationService;

    public RouteController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RouteDto> getAll() {
        return recommendationService.getAllRoutes();
    }
}
