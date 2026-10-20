package com.crowdsense.model;

import java.util.Objects;

/**
 * A physical boarding/alighting point.
 */
public class Stop {

    private final Long id;
    private final String name;
    private final String code;
    private final int sequence;
    private final double distanceFromStartKm;

    public Stop(Long id, String name, String code, int sequence, double distanceFromStartKm) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Stop name is required");
        this.code = code == null ? "" : code;
        this.sequence = sequence;
        this.distanceFromStartKm = distanceFromStartKm;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    /** Position of this stop along its route (1-based). */
    public int getSequence() {
        return sequence;
    }

    public double getDistanceFromStartKm() {
        return distanceFromStartKm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stop other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
