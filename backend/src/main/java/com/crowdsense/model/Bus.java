package com.crowdsense.model;

/**
 * A bus. Extends {@link TransportVehicle} so that buses and metros share the
 * same contract while still being distinguishable at runtime (polymorphism).
 */
public class Bus extends TransportVehicle {

    public Bus(Long id, String number, int capacity, String routeCode, String routeName) {
        super(id, number, capacity, routeCode, routeName);
    }

    @Override
    public String getTransportMode() {
        return "BUS";
    }

    @Override
    public String getDisplayName() {
        return "Bus " + getNumber() + " (" + getRouteCode() + ")";
    }
}
