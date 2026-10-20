package com.crowdsense.controller;

import com.crowdsense.dto.BusDto;
import com.crowdsense.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buses/search")
public class BusSearchController {

    private final RecommendationService recommendationService;

    public BusSearchController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Searches for buses that can serve the requested source -> destination.
     * Returns full bus details with current occupancy.
     */
    @GetMapping
    public List<BusDto> search(@RequestParam String source, @RequestParam String destination) {
        // Reuse recommendation logic to get the available set
        var resp = recommendationService.recommend(source, destination, null);
        return resp.availableBuses().stream()
                .map(item -> new BusDto(
                        item.busId(),
                        item.busNumber(),
                        item.routeCode(),
                        item.routeName(),
                        item.transportMode(),
                        null,
                        item.boardStopId(),
                        item.boardStopName(),
                        item.occupancyPercent(),
                        item.crowdCategory(),
                        null,
                        item.occupancyStale(),
                        item.occupancyMissing()
                ))
                .toList();
    }
}
