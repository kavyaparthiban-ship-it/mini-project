package com.crowdsense.model;

import java.util.Collections;
import java.util.List;

/**
 * One ranked travel option produced by the recommendation engine.
 *
 * <p>This is the domain object; the controller layer maps it into a DTO. It
 * carries not only the bus but also the reasoning (breakdown + explanation)
 * so the UI can justify the recommendation to the passenger.</p>
 */
public class Recommendation {

    private final Bus bus;
    private final Occupancy occupancy;
    private final CrowdCategory crowdCategory;
    private final Long boardStopId;
    private final String boardStopName;
    private final Long destinationStopId;
    private final double walkDistanceKm;
    private final int estimatedWaitMinutes;
    private final int estimatedTravelMinutes;
    private final double score;
    private final boolean recommended;
    private final double crowdScore;
    private final double waitScore;
    private final double travelScore;
    private final double walkScore;
    private final List<String> reasons;

    public Recommendation(Bus bus,
                          Occupancy occupancy,
                          CrowdCategory crowdCategory,
                          Long boardStopId,
                          String boardStopName,
                          Long destinationStopId,
                          double walkDistanceKm,
                          int estimatedWaitMinutes,
                          int estimatedTravelMinutes,
                          double score,
                          boolean recommended,
                          double crowdScore,
                          double waitScore,
                          double travelScore,
                          double walkScore,
                          List<String> reasons) {
        this.bus = bus;
        this.occupancy = occupancy;
        this.crowdCategory = crowdCategory;
        this.boardStopId = boardStopId;
        this.boardStopName = boardStopName;
        this.destinationStopId = destinationStopId;
        this.walkDistanceKm = walkDistanceKm;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.estimatedTravelMinutes = estimatedTravelMinutes;
        this.score = score;
        this.recommended = recommended;
        this.crowdScore = crowdScore;
        this.waitScore = waitScore;
        this.travelScore = travelScore;
        this.walkScore = walkScore;
        this.reasons = List.copyOf(reasons);
    }

    public Bus getBus() {
        return bus;
    }

    public Occupancy getOccupancy() {
        return occupancy;
    }

    public CrowdCategory getCrowdCategory() {
        return crowdCategory;
    }

    public Long getBoardStopId() {
        return boardStopId;
    }

    public String getBoardStopName() {
        return boardStopName;
    }

    public Long getDestinationStopId() {
        return destinationStopId;
    }

    public double getWalkDistanceKm() {
        return walkDistanceKm;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public int getEstimatedTravelMinutes() {
        return estimatedTravelMinutes;
    }

    /** Overall score in [0, 1]; higher is better. */
    public double getScore() {
        return score;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public double getCrowdScore() {
        return crowdScore;
    }

    public double getWaitScore() {
        return waitScore;
    }

    public double getTravelScore() {
        return travelScore;
    }

    public double getWalkScore() {
        return walkScore;
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }

    @Override
    public String toString() {
        return String.format("%s score=%.3f crowd=%s wait=%dm travel=%dm",
                bus.getDisplayName(), score, crowdCategory, estimatedWaitMinutes, estimatedTravelMinutes);
    }
}
