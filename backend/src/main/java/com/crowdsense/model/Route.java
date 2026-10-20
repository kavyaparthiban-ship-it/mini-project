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
     * Routes are modeled as serving both directions, so distance is independent
     * of which stop comes first in the stored route order.
     *
     * @return the distance, or {@code -1} when either stop is missing
     */
    public double distanceBetween(Long sourceId, Long destinationId) {
        Stop source = findStop(sourceId);
        Stop destination = findStop(destinationId);
        if (source == null || destination == null) {
            return -1;
        }
        return Math.abs(destination.getDistanceFromStartKm() - source.getDistanceFromStartKm());
    }

    /**
     * Stops between the given stops, in the direction of travel, including both
     * endpoints. Used by the route-diagram view.
     */
    public List<Stop> segment(Long fromStopId, Long toStopId) {
        Stop from = findStop(fromStopId);
        Stop to = findStop(toStopId);
        if (from == null || to == null) {
            return List.of();
        }
        int firstSequence = Math.min(from.getSequence(), to.getSequence());
        int lastSequence = Math.max(from.getSequence(), to.getSequence());
        List<Stop> segment = stops.stream()
                .filter(s -> s.getSequence() >= firstSequence && s.getSequence() <= lastSequence)
                .toList();
        if (from.getSequence() < to.getSequence()) {
            return segment;
        }
        List<Stop> reverseSegment = new ArrayList<>(segment);
        Collections.reverse(reverseSegment);
        return reverseSegment;
    }

    @Override
    public String toString() {
        return routeCode + " - " + routeName;
    }
}
