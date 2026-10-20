package com.crowdsense.model;

/**
 * The weights a passenger assigns to the factors the recommendation engine
 * considers. Higher weight means the passenger cares more about that factor.
 *
 * <p>Demonstrates encapsulation: values are clamped to a documented range so
 * the scoring formula always receives sane inputs, and
 * {@link #normalizedWeights()} converts them into shares that sum to 1.</p>
 */
public class PassengerPreference {

    /** Sensible default profile used when a passenger has not configured one. */
    public static final PassengerPreference DEFAULT = new PassengerPreference(5, 5, 5, 5, 1.5);

    private final int lowCrowdWeight;
    private final int shortWaitWeight;
    private final int shortTravelWeight;
    private final int shortWalkWeight;
    private final double maxWalkDistanceKm;

    public PassengerPreference(int lowCrowdWeight,
                               int shortWaitWeight,
                               int shortTravelWeight,
                               int shortWalkWeight,
                               double maxWalkDistanceKm) {
        this.lowCrowdWeight = clamp(lowCrowdWeight);
        this.shortWaitWeight = clamp(shortWaitWeight);
        this.shortTravelWeight = clamp(shortTravelWeight);
        this.shortWalkWeight = clamp(shortWalkWeight);
        this.maxWalkDistanceKm = maxWalkDistanceKm <= 0 ? 1.5 : maxWalkDistanceKm;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 9);
    }

    public int getLowCrowdWeight() {
        return lowCrowdWeight;
    }

    public int getShortWaitWeight() {
        return shortWaitWeight;
    }

    public int getShortTravelWeight() {
        return shortTravelWeight;
    }

    public int getShortWalkWeight() {
        return shortWalkWeight;
    }

    public double getMaxWalkDistanceKm() {
        return maxWalkDistanceKm;
    }

    /**
     * Converts the four raw weights into fractions that sum to 1.0.
     * When every weight is zero (passenger cares about nothing), falls back to
     * an equal split so the engine still produces a meaningful ranking.
     *
     * @return array in the order [crowd, wait, travel, walk]
     */
    public double[] normalizedWeights() {
        double total = lowCrowdWeight + shortWaitWeight + shortTravelWeight + shortWalkWeight;
        if (total <= 0) {
            return new double[] {0.25, 0.25, 0.25, 0.25};
        }
        return new double[] {
                lowCrowdWeight / total,
                shortWaitWeight / total,
                shortTravelWeight / total,
                shortWalkWeight / total
        };
    }

    @Override
    public String toString() {
        return String.format("Preference{crowd=%d, wait=%d, travel=%d, walk=%d, maxWalk=%.2fkm}",
                lowCrowdWeight, shortWaitWeight, shortTravelWeight, shortWalkWeight, maxWalkDistanceKm);
    }
}
