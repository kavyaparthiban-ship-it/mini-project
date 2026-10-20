package com.crowdsense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA mapping for the {@code occupancy_record} table (simulated readings).
 */
@Entity
@Table(name = "occupancy_record")
public class OccupancyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "occupancy_id")
    private Long occupancyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private BusEntity bus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stop_id", nullable = false)
    private StopEntity stop;

    @Column(name = "occupancy_percent", nullable = false)
    private java.math.BigDecimal occupancyPercent;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    protected OccupancyEntity() {
    }

    public Long getOccupancyId() {
        return occupancyId;
    }

    public void setOccupancyId(Long occupancyId) {
        this.occupancyId = occupancyId;
    }

    public BusEntity getBus() {
        return bus;
    }

    public void setBus(BusEntity bus) {
        this.bus = bus;
    }

    public StopEntity getStop() {
        return stop;
    }

    public void setStop(StopEntity stop) {
        this.stop = stop;
    }

    public Double getOccupancyPercent() {
        return occupancyPercent == null ? null : occupancyPercent.doubleValue();
    }

    public void setOccupancyPercent(Double occupancyPercent) {
        this.occupancyPercent = occupancyPercent == null ? null : java.math.BigDecimal.valueOf(occupancyPercent);
    }

    public java.math.BigDecimal getOccupancyPercentDecimal() {
        return occupancyPercent;
    }

    public void setOccupancyPercentDecimal(java.math.BigDecimal occupancyPercent) {
        this.occupancyPercent = occupancyPercent;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
