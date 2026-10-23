package com.crowdsense.service;

import com.crowdsense.config.CrowdSenseProperties;
import com.crowdsense.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Recommendation engine: finds feasible alternatives, normalizes factors,
 * applies passenger preferences, ranks deterministically, and explains the result.
 *
 * <p>Scoring (documented and configurable): higher score = better recommendation.</p>
 * <ul>
 *   <li>Crowd: {@code 1 - occupancyPercent/100} when present; worst when missing/stale (0.0)
 *     so unknown crowding does not get an unfair boost.</li>
 *   <li>Wait: normalized by headway (e.g. headway=12 min -> wait of 12 gets 0.5).</li>
 *   <li>Travel: normalized by distance/speed (shorter trip = higher score).</li>
 *   <li>Walk: normalized by distance to board stop (closer = higher score, penalized if over max walk).</li>
 * </ul>
 */
@Service
public class RecommendationEngineService {

    private final CrowdSenseProperties properties;

    public RecommendationEngineService(CrowdSenseProperties properties) {
        this.properties = properties;
    }

    /**
     * Ranks candidates and marks the top-scoring one as recommended. Ties are
     * broken deterministically by: lower occupancy, lower wait time, lower travel time,
     * lower walk distance, then by bus number.
     */
    public List<Recommendation> rank(List<Candidate> candidates, PassengerPreference preference) {
        PassengerPreference pref = preference == null ? PassengerPreference.DEFAULT : preference;
        double[] w = pref.normalizedWeights();
        double wCrowd = w[0];
        double wWait = w[1];
        double wTravel = w[2];
        double wWalk = w[3];

        // Global normalization references so the scores are interpretable across options
        int maxWait = candidates.stream()
                .mapToInt(Candidate::estimatedWaitMinutes)
                .max().orElse(properties.getWaiting().getAvgHeadwayMinutes() * 2);
        double maxDistance = candidates.stream()
                .mapToDouble(Candidate::tripDistanceKm)
                .max().orElse(10.0);

        List<Scored> scored = new ArrayList<>();
        for (Candidate c : candidates) {
            Occupancy occ = c.occupancy();
            // Unknown/stale crowding must not be treated as "less crowded" - punish heavily
            double crowdRaw = (occ == null || occ.isMissing() || occ.isStale())
                    ? 0.0
                    : Math.max(0.0, 1.0 - occ.getOccupancyPercent() / 100.0);
            CrowdCategory category = occ == null ? CrowdCategory.UNKNOWN : occ.getCrowdCategory();

            double waitRaw = maxWait <= 0
                    ? 1.0
                    : Math.max(0.0, 1.0 - (double) c.estimatedWaitMinutes() / (double) (maxWait + 1));
            double travelRaw = maxDistance <= 0
                    ? 1.0
                    : Math.max(0.0, 1.0 - c.tripDistanceKm() / (maxDistance + 0.1));
            double walkRaw = walkScore(c.walkDistanceKm(), pref.getMaxWalkDistanceKm());

            double score = wCrowd * crowdRaw + wWait * waitRaw + wTravel * travelRaw + wWalk * walkRaw;
            if (score < 0) {
                score = 0;
            }
            if (score > 1) {
                score = 1;
            }

            List<String> reasons = buildReasons(c, occ, category, crowdRaw, waitRaw, travelRaw, walkRaw);
            scored.add(new Scored(c, category, score, crowdRaw, waitRaw, travelRaw, walkRaw, reasons));
        }

        // Deterministic ranking with tie-breakers
        scored.sort(Comparator
                .comparingDouble(Scored::score).reversed()
                .thenComparing(s -> occupancyValue(s.candidate.occupancy()))
                .thenComparing(s -> s.candidate.estimatedWaitMinutes())
                .thenComparing(s -> s.candidate.estimatedTravelMinutes())
                .thenComparing(s -> s.candidate.walkDistanceKm())
                .thenComparing(s -> s.candidate.vehicle().getNumber()));

        List<Recommendation> result = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            Scored s = scored.get(i);
            Candidate c = s.candidate;
            boolean recommended = i == 0 && !scored.isEmpty();
            Bus busForRec = c.vehicle() instanceof Bus ? (Bus) c.vehicle() : new Bus(c.vehicle().getId(), c.vehicle().getNumber(), c.vehicle().getCapacity(), c.vehicle().getRouteCode(), c.vehicle().getRouteName());
            result.add(new Recommendation(
                    busForRec,
                    c.occupancy(),
                    s.category,
                    c.boardStopId(),
                    c.boardStopName(),
                    c.destinationStopId(),
                    c.walkDistanceKm(),
                    c.estimatedWaitMinutes(),
                    c.estimatedTravelMinutes(),
                    s.score(),
                    recommended,
                    s.crowdScore(),
                    s.waitScore(),
                    s.travelScore(),
                    s.walkScore(),
                    s.reasons(),
                    c.itinerary()
            ));
        }
        return result;
    }

    private double walkScore(double walkDistanceKm, double maxWalkKm) {
        if (walkDistanceKm <= 0) {
            return 1.0;
        }
        if (walkDistanceKm <= maxWalkKm) {
            return Math.max(0.0, 1.0 - walkDistanceKm / (maxWalkKm * 2.0));
        }
        // Over the preferred max walk distance - heavily penalised but not impossible
        return Math.max(0.0, 0.4 - (walkDistanceKm - maxWalkKm) / 10.0);
    }

    private double occupancyValue(Occupancy occ) {
        if (occ == null || occ.isMissing()) {
            return 1000.0;
        }
        Double p = occ.getOccupancyPercent();
        return p == null ? 1000.0 : p;
    }

    private List<String> buildReasons(Candidate c, Occupancy occ, CrowdCategory category,
                                      double crowdRaw, double waitRaw, double travelRaw, double walkRaw) {
        List<String> reasons = new ArrayList<>();
        if (occ != null && !occ.isMissing() && !occ.isStale() && occ.getOccupancyPercent() != null) {
            if (occ.getOccupancyPercent() < 45) {
                reasons.add(String.format("Less crowded than alternatives (%d%% occupancy)",
                        Math.round(occ.getOccupancyPercent())));
            } else if (category == CrowdCategory.LOW) {
                reasons.add("Low crowd level observed in simulated data.");
            }
        }
        if (c.estimatedWaitMinutes() <= 6) {
            reasons.add(String.format("Short estimated wait (%d min).", c.estimatedWaitMinutes()));
        } else if (c.estimatedWaitMinutes() <= 9) {
            reasons.add(String.format("Reasonable estimated wait (%d min).", c.estimatedWaitMinutes()));
        }
        if (c.tripDistanceKm() <= 2.5) {
            reasons.add("Shorter trip distance.");
        }
        if (c.walkDistanceKm() <= 0.3) {
            reasons.add("Very close to alternative stop.");
        } else if (c.walkDistanceKm() <= 0.8) {
            reasons.add("Walkable distance to alternative stop.");
        }
        if (c.itinerary().size() > 1) {
            String transferStops = c.itinerary().subList(0, c.itinerary().size() - 1).stream()
                    .map(RecommendationLeg::toStopName)
                    .collect(java.util.stream.Collectors.joining(", "));
            int transferCount = c.itinerary().size() - 1;
            reasons.add("Requires " + transferCount + (transferCount == 1 ? " transfer at " : " transfers at ")
                    + transferStops + ".");
        }
        if (reasons.isEmpty()) {
            reasons.add("Ranked by overall preference-weighted score.");
        }
        return reasons;
    }

    /** Candidate option passed to the engine before scoring. */
    public record Candidate(
            TransportVehicle vehicle,
            Occupancy occupancy,
            Long boardStopId,
            String boardStopName,
            Long destinationStopId,
            double walkDistanceKm,
            int estimatedWaitMinutes,
            int estimatedTravelMinutes,
            double tripDistanceKm,
            List<RecommendationLeg> itinerary
    ) {
        public Candidate(TransportVehicle vehicle,
                         Occupancy occupancy,
                         Long boardStopId,
                         String boardStopName,
                         Long destinationStopId,
                         double walkDistanceKm,
                         int estimatedWaitMinutes,
                         int estimatedTravelMinutes,
                         double tripDistanceKm) {
            this(vehicle, occupancy, boardStopId, boardStopName, destinationStopId,
                    walkDistanceKm, estimatedWaitMinutes, estimatedTravelMinutes, tripDistanceKm, List.of());
        }

        public Candidate {
            itinerary = List.copyOf(itinerary);
        }
    }

    private record Scored(
            Candidate candidate,
            CrowdCategory category,
            double score,
            double crowdScore,
            double waitScore,
            double travelScore,
            double walkScore,
            List<String> reasons
    ) {
    }

    public double getWeightCrowd() {
        return properties.getRecommendation().getWeightCrowd();
    }

    public double getWeightWait() {
        return properties.getRecommendation().getWeightWait();
    }

    public double getWeightTravel() {
        return properties.getRecommendation().getWeightTravel();
    }

    public double getWeightWalk() {
        return properties.getRecommendation().getWeightWalk();
    }
}
