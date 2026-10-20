package com.crowdsense.model;

/**
 * Abstract base class for every object that lives on the transport network
 * (buses, metro vehicles, and future transport modes).
 *
 * <p>Demonstrates abstraction and inheritance: it captures the state every
 * transport vehicle shares, while leaving the mode-specific behaviour to
 * subclasses through {@link #getTransportMode()} and {@link #getDisplayName()}.</p>
 */
public abstract class TransportVehicle {

    private final Long id;
    private final String number;
    private final int capacity;
    private final String routeCode;
    private final String routeName;

    protected TransportVehicle(Long id,
                               String number,
                               int capacity,
                               String routeCode,
                               String routeName) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Vehicle number must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Vehicle capacity must be positive");
        }
        this.id = id;
        this.number = number;
        this.capacity = capacity;
        this.routeCode = routeCode;
        this.routeName = routeName;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    /** The transport mode, e.g. BUS or METRO. */
    public abstract String getTransportMode();

    /** A human readable name such as "Bus 102 on R102". */
    public abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}
