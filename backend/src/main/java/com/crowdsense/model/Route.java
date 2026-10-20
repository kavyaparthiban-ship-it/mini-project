package com.crowdsense.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered path through a set of {@link Stop}s served by one or more vehicles.
 *
 * <p>Provides the geography the recommendation engine needs: is a stop on this
 * route, and in what order does a passenger travel between two stops.</p>
 */
public class Route {

    private final Long id;
    private final String routeCode;
    private final String routeName;
    private final String transportMode;
    private final List<Stop> stops;

    public Route(Long id, String routeCode, String routeName, String transportMode, List<Stop> stops) {
        this.id = id;
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.transportMode = transportMode;
        this.stops = new ArrayList<>(stops);
        this.stops.sort((a, b) -> Integer.compare(a.getSequence(), b.getSequence()));
    }

    public Long getId() {
        return id;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public List<Stop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    /** @return the stop with the given id, or {@code null} if it is not on this route */
    public Stop findStop(Long stopId) {
        return stops.stream()
                .filter(s -> s.getId() != null && s.getId().equals(stopId))
                .findFirst()
                .orElse(null);
    }

    /** @return true when the route visits the given stop */
    public boolean serves(Long stopId) {
        return findStop(stopId) != null;
    }

    /**
     * Travel distance in kilometres between two stops on this route.
     *
     * @return the distance, or {@code -1} when either stop is missing or the
     *         destination does not come after the source (i.e. not a valid leg)
     */
    public double distanceBetween(Long sourceId, Long destinationId) {
        Stop source = findStop(sourceId);
        Stop destination = findStop(destinationId);
        if (source == null || destination == null) {
            return -1;
        }
        if (destination.getSequence() <= source.getSequence()) {
            return -1;
        }
        return destination.getDistanceFromStartKm() - source.getDistanceFromStartKm();
    }

    /**
     * Stops visited strictly after the given stop up to and including the destination.
     * Used by the route-diagram view.
     */
    public List<Stop> segment(Long fromStopId, Long toStopId) {
        Stop from = findStop(fromStopId);
        Stop to = findStop(toStopId);
        if (from == null || to == null || to.getSequence() < from.getSequence()) {
            return List.of();
        }
        return stops.stream()
                .filter(s -> s.getSequence() >= from.getSequence() && s.getSequence() <= to.getSequence())
                .toList();
    }

    @Override
    public String toString() {
        return routeCode + " - " + routeName;
    }
}
