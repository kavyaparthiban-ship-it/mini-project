package com.crowdsense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * JPA mapping for the {@code passenger_preference} table.
 */
@Entity
@Table(name = "passenger_preference")
public class PreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false, unique = true)
    private PassengerEntity passenger;

    @Column(name = "prefer_low_crowd", nullable = false)
    private Integer preferLowCrowd;

    @Column(name = "prefer_short_wait", nullable = false)
    private Integer preferShortWait;

    @Column(name = "prefer_short_travel", nullable = false)
    private Integer preferShortTravel;

    @Column(name = "prefer_short_walk", nullable = false)
    private Integer preferShortWalk;

    @Column(name = "max_walk_distance_km", nullable = false)
    private Double maxWalkDistanceKm;

    protected PreferenceEntity() {
    }

    public Long getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

    public PassengerEntity getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerEntity passenger) {
        this.passenger = passenger;
    }

    public Integer getPreferLowCrowd() {
        return preferLowCrowd;
    }

    public void setPreferLowCrowd(Integer preferLowCrowd) {
        this.preferLowCrowd = preferLowCrowd;
    }

    public Integer getPreferShortWait() {
        return preferShortWait;
    }

    public void setPreferShortWait(Integer preferShortWait) {
        this.preferShortWait = preferShortWait;
    }

    public Integer getPreferShortTravel() {
        return preferShortTravel;
    }

    public void setPreferShortTravel(Integer preferShortTravel) {
        this.preferShortTravel = preferShortTravel;
    }

    public Integer getPreferShortWalk() {
        return preferShortWalk;
    }

    public void setPreferShortWalk(Integer preferShortWalk) {
        this.preferShortWalk = preferShortWalk;
    }

    public Double getMaxWalkDistanceKm() {
        return maxWalkDistanceKm;
    }

    public void setMaxWalkDistanceKm(Double maxWalkDistanceKm) {
        this.maxWalkDistanceKm = maxWalkDistanceKm;
    }
}
