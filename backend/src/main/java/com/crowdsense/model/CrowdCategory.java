package com.crowdsense.model;

/**
 * Crowd category derived from an occupancy percentage.
 *
 * <p>This is an abstraction over the raw percentage so that every part of the
 * system (classification, scoring, UI labels) speaks the same language.</p>
 *
 * <p>Thresholds are supplied from configuration, but the enum also defines the
 * documented defaults so the classification rules are visible in one place and
 * can be unit-tested without Spring.</p>
 */
public enum CrowdCategory {

    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High"),
    CRITICAL("Critical"),
    UNKNOWN("Unknown");

    /** Default lower bound (percentage) for each category. */
    public static final double DEFAULT_MODERATE_MIN = 40.0;
    public static final double DEFAULT_HIGH_MIN = 65.0;
    public static final double DEFAULT_CRITICAL_MIN = 85.0;

    private final String label;

    CrowdCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Classifies an occupancy percentage using the documented default thresholds.
     *
     * @param occupancyPercent occupancy in [0, 100]; {@code null} yields {@link #UNKNOWN}
     * @return the matching category, or {@link #UNKNOWN} when the value is missing
     */
    public static CrowdCategory classify(Double occupancyPercent) {
        return classify(occupancyPercent, DEFAULT_MODERATE_MIN, DEFAULT_HIGH_MIN, DEFAULT_CRITICAL_MIN);
    }

    /**
     * Classifies an occupancy percentage using explicit, configurable thresholds.
     *
     * @param occupancyPercent occupancy in [0, 100]; {@code null} yields {@link #UNKNOWN}
     * @param moderateMin      lower bound of MODERATE
     * @param highMin          lower bound of HIGH
     * @param criticalMin      lower bound of CRITICAL
     * @return the matching category, or {@link #UNKNOWN} when the value is missing
     */
    public static CrowdCategory classify(Double occupancyPercent,
                                         double moderateMin,
                                         double highMin,
                                         double criticalMin) {
        if (occupancyPercent == null) {
            return UNKNOWN;
        }
        if (occupancyPercent >= criticalMin) {
            return CRITICAL;
        }
        if (occupancyPercent >= highMin) {
            return HIGH;
        }
        if (occupancyPercent >= moderateMin) {
            return MODERATE;
        }
        return LOW;
    }

    /**
     * Whether this category is at least "high" on the crowding scale.
     * Used by the recommendation engine to decide if an alternative is worth suggesting.
     */
    public boolean isOvercrowded() {
        return this == HIGH || this == CRITICAL;
    }
}
