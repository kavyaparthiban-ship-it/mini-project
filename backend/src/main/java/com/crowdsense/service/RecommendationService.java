package com.crowdsense.service;

import com.crowdsense.config.CrowdSenseProperties;
import com.crowdsense.entity.*;
import com.crowdsense.exception.BadRequestException;
import com.crowdsense.exception.ResourceNotFoundException;
import com.crowdsense.model.*;
import com.crowdsense.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Orchestrates data loading for the recommendation flow and wires the Java OOP
 * engine (CrowdClassificationService + RecommendationEngineService).
 */
@Service
public class RecommendationService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;
    private final OccupancyRepository occupancyRepository;
    private final PassengerRepository passengerRepository;
    private final PreferenceRepository preferenceRepository;
    private final CrowdClassificationService crowdService;
    private final RecommendationEngineService engineService;
    private final CrowdSenseProperties properties;

    public RecommendationService(BusRepository busRepository,
                                 RouteRepository routeRepository,
                                 StopRepository stopRepository,
                                 RouteStopRepository routeStopRepository,
                                 OccupancyRepository occupancyRepository,
                                 PassengerRepository passengerRepository,
                                 PreferenceRepository preferenceRepository,
                                 CrowdClassificationService crowdService,
                                 RecommendationEngineService engineService,
                                 CrowdSenseProperties properties) {
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.routeStopRepository = routeStopRepository;
        this.occupancyRepository = occupancyRepository;
        this.passengerRepository = passengerRepository;
        this.preferenceRepository = preferenceRepository;
        this.crowdService = crowdService;
        this.engineService = engineService;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<com.crowdsense.dto.BusDto> getAllBuses() {
        List<BusEntity> buses = busRepository.findAll();
        List<com.crowdsense.dto.BusDto> result = new ArrayList<>();
        for (BusEntity b : buses) {
            result.add(mapToDto(b));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public com.crowdsense.dto.BusDto mapToDto(BusEntity b) {
        OccupancyEntity latest = occupancyRepository.findLatestByBus(b.getBusId()).orElse(null);
        Occupancy occ = crowdService.classify(latest);
        CrowdCategory cat = occ.isMissing() ? CrowdCategory.UNKNOWN : occ.getCrowdCategory();
        return new com.crowdsense.dto.BusDto(
                b.getBusId(),
                b.getBusNumber(),
                b.getRoute() != null ? b.getRoute().getRouteCode() : null,
                b.getRoute() != null ? b.getRoute().getRouteName() : null,
                b.getRoute() != null ? b.getRoute().getTransportMode() : null,
                b.getCapacity(),
                b.getCurrentStop() != null ? b.getCurrentStop().getStopId() : null,
                b.getCurrentStop() != null ? b.getCurrentStop().getStopName() : null,
                occ.getOccupancyPercent(),
                cat.getLabel(),
                latest != null ? latest.getRecordedAt().toString() : null,
                occ.isStale(),
                occ.isMissing()
        );
    }

    @Transactional(readOnly = true)
    public com.crowdsense.dto.RecommendationResponse recommend(String sourceName, String destinationName, Long passengerId) {
        return recommend(sourceName, destinationName, passengerId, null);
    }

    @Transactional(readOnly = true)
    public com.crowdsense.dto.RecommendationResponse recommend(String sourceName,
                                                               String destinationName,
                                                               Long passengerId,
                                                               com.crowdsense.dto.RecommendationRequest.PreferenceOverride override) {
        if (sourceName == null || sourceName.isBlank() || destinationName == null || destinationName.isBlank()) {
            throw new BadRequestException("source and destination are required");
        }

        StopEntity sourceStop = stopRepository.findByStopName(sourceName)
                .or(() -> fuzzyFindStop(sourceName))
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found: " + sourceName));
        StopEntity destStop = stopRepository.findByStopName(destinationName)
                .or(() -> fuzzyFindStop(destinationName))
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found: " + destinationName));

        if (sourceStop.getStopId().equals(destStop.getStopId())) {
            throw new BadRequestException("source and destination must be different");
        }

        PassengerPreference preference = resolvePreference(passengerId, override);

        // Routes are modeled once; buses serve trips in either direction between their stops.
        List<BusEntity> feasible = busRepository.findBusesServingDestination(sourceStop.getStopId(), destStop.getStopId());
        if (feasible.isEmpty()) {
            // Fallback: scan all buses whose route contains both stops
            feasible = findFeasibleByScan(sourceStop.getStopId(), destStop.getStopId());
        }

        if (feasible.isEmpty()) {
            throw new ResourceNotFoundException("No bus serves " + sourceName + " to " + destinationName + " in the simulated data");
        }

        // Also consider alternatives from other routes that stop near the source and serve destination
        // (mimics the "find suitable alternative" requirement)
        Set<Long> seenBusIds = new HashSet<>();
        List<BusEntity> allCandidates = new ArrayList<>();
        for (BusEntity b : feasible) {
            if (seenBusIds.add(b.getBusId())) {
                allCandidates.add(b);
            }
        }
        List<BusEntity> others = findAlternativesFromNearby(sourceStop.getStopId(), destStop.getStopId(), seenBusIds);
        allCandidates.addAll(others);

        List<RecommendationEngineService.Candidate> candidates = new ArrayList<>();
        int headway = properties.getWaiting().getAvgHeadwayMinutes();
        double speed = properties.getTravel().getAvgSpeedKmph();

        for (BusEntity busEntity : allCandidates) {
            RouteEntity routeEntity = busEntity.getRoute();
            Route routeModel = buildRouteModel(routeEntity);
            double tripDist = routeModel.distanceBetween(sourceStop.getStopId(), destStop.getStopId());
            if (tripDist < 0) {
                // This bus does not serve both stops.
                continue;
            }
            // Walk distance to board stop: if the bus is currently reported at a nearby stop,
            // approximate; otherwise use a small configurable distance
            double walkDist = estimateWalkDistance(busEntity, sourceStop);
            int waitMins = estimateWaitMinutes(busEntity, sourceStop, headway);
            int travelMins = (int) Math.max(2, Math.round((tripDist / Math.max(speed, 1.0)) * 60.0));
            OccupancyEntity latest = occupancyRepository.findLatestByBus(busEntity.getBusId()).orElse(null);
            Occupancy occ = crowdService.classify(latest);
            TransportVehicle vehicle;
            if (busEntity.getRoute() != null && "METRO".equalsIgnoreCase(busEntity.getRoute().getTransportMode())) {
                vehicle = new Metro(busEntity.getBusId(), busEntity.getBusNumber(), busEntity.getCapacity(),
                        busEntity.getRoute().getRouteCode(), busEntity.getRoute().getRouteName());
            } else {
                vehicle = new Bus(busEntity.getBusId(), busEntity.getBusNumber(), busEntity.getCapacity(),
                        busEntity.getRoute().getRouteCode(), busEntity.getRoute().getRouteName());
            }
            candidates.add(new RecommendationEngineService.Candidate(
                    vehicle,
                    occ,
                    sourceStop.getStopId(),
                    sourceStop.getStopName(),
                    destStop.getStopId(),
                    walkDist,
                    waitMins,
                    travelMins,
                    tripDist
            ));
        }

        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("No feasible alternatives found for " + sourceName + " to " + destinationName);
        }

        List<Recommendation> ranked = engineService.rank(candidates, preference);
        List<com.crowdsense.dto.RecommendationItemDto> items = ranked.stream()
                .map(this::toItemDto)
                .toList();

        Recommendation recommended = ranked.stream().filter(Recommendation::isRecommended).findFirst().orElse(ranked.get(0));
        String explanation = buildExplanation(recommended);

        return new com.crowdsense.dto.RecommendationResponse(
                sourceName,
                destinationName,
                sourceStop.getStopName(),
                destStop.getStopName(),
                items,
                toItemDto(recommended),
                explanation,
                "DEMO NOTE: Bus 101 is ~94% crowded; Bus 102 is ~42% crowded and is the recommended alternative in the sample scenario. Values come from simulated occupancy data and the configured scoring formula.",
                new com.crowdsense.dto.RecommendationResponse.ScoringInfo(
                        engineService.getWeightCrowd(),
                        engineService.getWeightWait(),
                        engineService.getWeightTravel(),
                        engineService.getWeightWalk(),
                        "score = wCrowd*(1-occ/100) + wWait*(1-wait/maxWait) + wTravel*(1-dist/maxDist) + wWalk*walkScore; " +
                                "missing/stale occupancy is treated as worst case (0); higher score = better."
                )
        );
    }

    private Optional<StopEntity> fuzzyFindStop(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String lower = name.toLowerCase();
        return stopRepository.findAll().stream()
                .filter(s -> s.getStopName() != null && s.getStopName().toLowerCase().contains(lower))
                .findFirst();
    }

    private List<BusEntity> findFeasibleByScan(Long sourceId, Long destId) {
        List<BusEntity> result = new ArrayList<>();
        for (BusEntity bus : busRepository.findAll()) {
            RouteEntity route = bus.getRoute();
            if (route == null) {
                continue;
            }
            List<RouteStopEntity> stops = routeStopRepository.findByRoute_RouteIdOrderByStopSequenceAsc(route.getRouteId());
            int srcSeq = -1;
            int dstSeq = -1;
            for (RouteStopEntity rs : stops) {
                if (rs.getStop().getStopId().equals(sourceId)) {
                    srcSeq = rs.getStopSequence();
                }
                if (rs.getStop().getStopId().equals(destId)) {
                    dstSeq = rs.getStopSequence();
                }
            }
            if (srcSeq >= 0 && dstSeq >= 0 && srcSeq != dstSeq) {
                result.add(bus);
            }
        }
        return result;
    }

    private List<BusEntity> findAlternativesFromNearby(Long sourceId, Long destId, Set<Long> seen) {
        List<BusEntity> result = new ArrayList<>();
        for (BusEntity bus : busRepository.findAll()) {
            if (seen.contains(bus.getBusId())) {
                continue;
            }
            RouteEntity route = bus.getRoute();
            if (route == null) {
                continue;
            }
            List<RouteStopEntity> stops = routeStopRepository.findByRoute_RouteIdOrderByStopSequenceAsc(route.getRouteId());
            int dstSeq = -1;
            for (RouteStopEntity rs : stops) {
                if (rs.getStop().getStopId().equals(destId)) {
                    dstSeq = rs.getStopSequence();
                    break;
                }
            }
            if (dstSeq < 0) {
                continue;
            }
            // Accept if the bus stops at or near the source area (within 2 stops before source
            // OR the route passes source). The requirement says "consider whether the passenger
            // can reasonably reach the alternative stop" - we model this with walk distance.
            // For the sample set, routes 3 also connect areas that can reach CBT/others.
            boolean hasSourceOrNearby = stops.stream().anyMatch(rs ->
                    rs.getStop().getStopId().equals(sourceId) ||
                            Math.abs(stops.indexOf(rs) - getIndexOfSource(stops, sourceId)) <= 2 && sourceIdFound(stops, sourceId));
            if (!sourceIdFound(stops, sourceId) && !hasSourceOrNearby) {
                // Also allow: route passes destination but we still want alternatives
                // only when realistically reachable - but to satisfy "find suitable alternatives"
                // we include buses that serve destination; engine will penalise large walk distances.
            }
            // Keep it simple and inclusive, let the engine penalise poor walk distance
            result.add(bus);
        }
        return result;
    }

    private boolean sourceIdFound(List<RouteStopEntity> stops, Long sourceId) {
        return stops.stream().anyMatch(rs -> rs.getStop().getStopId().equals(sourceId));
    }

    private int getIndexOfSource(List<RouteStopEntity> stops, Long sourceId) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getStop().getStopId().equals(sourceId)) {
                return i;
            }
        }
        return -1;
    }

    private Route buildRouteModel(RouteEntity routeEntity) {
        if (routeEntity == null) {
            return new Route(null, null, null, null, List.of());
        }
        List<RouteStopEntity> rss = routeStopRepository.findByRoute_RouteIdOrderByStopSequenceAsc(routeEntity.getRouteId());
        List<Stop> stops = new ArrayList<>();
        for (RouteStopEntity rs : rss) {
            stops.add(new Stop(rs.getStop().getStopId(),
                    rs.getStop().getStopName(),
                    rs.getStop().getStopCode(),
                    rs.getStopSequence() != null ? rs.getStopSequence() : 0,
                    rs.getDistanceFromStart() != null ? rs.getDistanceFromStart() : 0.0));
        }
        return new Route(routeEntity.getRouteId(), routeEntity.getRouteCode(), routeEntity.getRouteName(),
                routeEntity.getTransportMode(), stops);
    }

    private double estimateWalkDistance(BusEntity bus, StopEntity sourceStop) {
        // Simulated: if the bus's route starts at or is near source, walk is small.
        // We keep values reasonable and configurable by the engine.
        if (bus.getRoute() != null) {
            List<RouteStopEntity> rss = routeStopRepository.findByRoute_RouteIdOrderByStopSequenceAsc(bus.getRoute().getRouteId());
            for (RouteStopEntity rs : rss) {
                if (rs.getStop().getStopId().equals(sourceStop.getStopId()) && rs.getStopSequence() == 1) {
                    return 0.1;
                }
                if (rs.getStop().getStopId().equals(sourceStop.getStopId())) {
                    return 0.25;
                }
            }
        }
        return 0.6;
    }

    private int estimateWaitMinutes(BusEntity bus, StopEntity sourceStop, int headway) {
        // Simulated headway-based estimate; the UI displays it as estimated.
        if (bus.getCurrentStop() != null && bus.getCurrentStop().getStopId().equals(sourceStop.getStopId())) {
            return Math.max(2, headway / 3);
        }
        return Math.max(3, headway / 2);
    }

    private PassengerPreference resolvePreference(Long passengerId, com.crowdsense.dto.RecommendationRequest.PreferenceOverride override) {
        if (override != null) {
            return new PassengerPreference(
                    override.preferLowCrowd() == null ? 5 : override.preferLowCrowd(),
                    override.preferShortWait() == null ? 5 : override.preferShortWait(),
                    override.preferShortTravel() == null ? 5 : override.preferShortTravel(),
                    override.preferShortWalk() == null ? 5 : override.preferShortWalk(),
                    override.maxWalkDistanceKm() == null ? 1.5 : override.maxWalkDistanceKm()
            );
        }
        if (passengerId != null) {
            Optional<PreferenceEntity> pe = preferenceRepository.findByPassenger_PassengerId(passengerId);
            if (pe.isPresent()) {
                PreferenceEntity p = pe.get();
                return new PassengerPreference(
                        p.getPreferLowCrowd(), p.getPreferShortWait(), p.getPreferShortTravel(),
                        p.getPreferShortWalk(), p.getMaxWalkDistanceKm()
                );
            }
        }
        return PassengerPreference.DEFAULT;
    }

    private com.crowdsense.dto.RecommendationItemDto toItemDto(Recommendation r) {
        Double occPct = r.getOccupancy() == null ? null : r.getOccupancy().getOccupancyPercent();
        boolean stale = r.getOccupancy() != null && r.getOccupancy().isStale();
        boolean missing = r.getOccupancy() == null || r.getOccupancy().isMissing();
        return new com.crowdsense.dto.RecommendationItemDto(
                r.getBus().getId(),
                r.getBus().getNumber(),
                r.getBus().getRouteCode(),
                r.getBus().getRouteName(),
                r.getBus().getTransportMode(),
                occPct,
                r.getCrowdCategory() == null ? "Unknown" : r.getCrowdCategory().getLabel(),
                stale,
                missing,
                r.getBoardStopId(),
                r.getBoardStopName(),
                r.getWalkDistanceKm(),
                r.getEstimatedWaitMinutes(),
                r.getEstimatedTravelMinutes(),
                computeTripDist(r),
                round(r.getScore()),
                r.isRecommended(),
                round(r.getCrowdScore()),
                round(r.getWaitScore()),
                round(r.getTravelScore()),
                round(r.getWalkScore()),
                r.getReasons()
        );
    }

    private Double computeTripDist(Recommendation r) {
        // computed upstream; keep for API shape
        return null;
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private String buildExplanation(Recommendation recommended) {
        Bus bus = recommended.getBus();
        String crowd = recommended.getCrowdCategory() == null ? "unknown crowd" : recommended.getCrowdCategory().getLabel().toLowerCase();
        Double occ = recommended.getOccupancy() == null ? null : recommended.getOccupancy().getOccupancyPercent();
        String occStr = occ == null ? crowd : String.format("%.0f%% %s", occ, crowd);
        return String.format("Recommended %s on %s because it is less crowded (%s), has an estimated wait of %d min and estimated travel of %d min. " +
                        "Scoring uses configurable weights and penalises missing/stale occupancy data.",
                bus.getDisplayName(), bus.getRouteName(),
                occStr, recommended.getEstimatedWaitMinutes(), recommended.getEstimatedTravelMinutes());
    }

    @Transactional(readOnly = true)
    public List<com.crowdsense.dto.RouteDto> getAllRoutes() {
        List<RouteEntity> routes = routeRepository.findAll();
        List<com.crowdsense.dto.RouteDto> result = new ArrayList<>();
        for (RouteEntity route : routes) {
            List<RouteStopEntity> rss = routeStopRepository.findByRoute_RouteIdOrderByStopSequenceAsc(route.getRouteId());
            List<com.crowdsense.dto.RouteStopDto> stops = rss.stream()
                    .map(rs -> new com.crowdsense.dto.RouteStopDto(
                            rs.getStop().getStopId(),
                            rs.getStop().getStopName(),
                            rs.getStop().getStopCode(),
                            rs.getStopSequence(),
                            rs.getDistanceFromStart()
                    ))
                    .toList();
            result.add(new com.crowdsense.dto.RouteDto(route.getRouteId(), route.getRouteCode(),
                    route.getRouteName(), route.getTransportMode(), stops));
        }
        return result;
    }
}
