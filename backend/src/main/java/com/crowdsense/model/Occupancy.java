package com.crowdsense.model;

/**
 * An immutable snapshot of how full a vehicle is at a point in time.
 *
 * <p>Demonstrates encapsulation: the raw passenger count and capacity are kept
 * private and only the derived, validated occupancy percentage is exposed.</p>
 */
public final class Occupancy {

    private final int passengerCount;
    private final int capacity;
    private final double occupancyPercent;
    private final boolean stale;

    private Occupancy(int passengerCount, int capacity, double occupancyPercent, boolean stale) {
        this.passengerCount = passengerCount;
        this.capacity = capacity;
        this.occupancyPercent = occupancyPercent;
        this.stale = stale;
    }

    /**
     * Builds an occupancy snapshot from a stored percentage.
     *
     * @param occupancyPercent percentage in [0, 100], or {@code null} when there is no reading
     * @param capacity         vehicle capacity, used to derive the passenger count
     * @param stale            whether the reading is older than the staleness limit
     * @return an {@link Occupancy}; the percentage is {@code null} when no reading exists
     */
    public static Occupancy fromPercent(Double occupancyPercent, int capacity, boolean stale) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (occupancyPercent == null) {
            return new Occupancy(0, capacity, Double.NaN, true);
        }
        if (occupancyPercent < 0 || occupancyPercent > 100) {
            throw new IllegalArgumentException("Occupancy percent must be between 0 and 100");
        }
        int passengers = (int) Math.round(capacity * occupancyPercent / 100.0);
        return new Occupancy(passengers, capacity, occupancyPercent, stale);
    }

    /** Builds an occupancy snapshot from a raw passenger count. */
    public static Occupancy fromCount(int passengerCount, int capacity, boolean stale) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (passengerCount < 0) {
            throw new IllegalArgumentException("Passenger count must not be negative");
        }
        double percent = Math.min(100.0, passengerCount * 100.0 / capacity);
        return new Occupancy(passengerCount, capacity, percent, stale);
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public int getCapacity() {
        return capacity;
    }

    /** True when there is no usable reading (missing or stale). */
    public boolean isMissing() {
        return Double.isNaN(occupancyPercent);
    }

    public boolean isStale() {
        return stale;
    }

    /**
     * @return the occupancy percentage, or {@code null} when no usable reading exists
     */
    public Double getOccupancyPercent() {
        return isMissing() ? null : occupancyPercent;
    }

    public CrowdCategory getCrowdCategory() {
        return CrowdCategory.classify(getOccupancyPercent());
    }

    @Override
    public String toString() {
        return isMissing()
                ? "Occupancy{no reading}"
                : String.format("Occupancy{%.1f%% (%d/%d)%s}",
                        occupancyPercent, passengerCount, capacity, stale ? " stale" : "");
    }
}
