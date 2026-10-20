package com.crowdsense.model;

/**
 * A metro vehicle. Provided so the system can compare multiple transport modes
 * (a scope item in the project report). It shares {@link TransportVehicle}
 * behaviour but reports a different mode.
 */
public class Metro extends TransportVehicle {

    public Metro(Long id, String number, int capacity, String routeCode, String routeName) {
        super(id, number, capacity, routeCode, routeName);
    }

    @Override
    public String getTransportMode() {
        return "METRO";
    }

    @Override
    public String getDisplayName() {
        return "Metro " + getNumber() + " (" + getRouteCode() + ")";
    }
}
