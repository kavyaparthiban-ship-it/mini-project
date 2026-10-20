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
 * JPA mapping for the {@code confirmed_journey} table.
 */
@Entity
@Table(name = "confirmed_journey")
public class ConfirmedJourneyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journey_id")
    private Long journeyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passenger_id", nullable = false)
    private PassengerEntity passenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_bus_id", nullable = false)
    private BusEntity selectedBus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_stop_id", nullable = false)
    private StopEntity sourceStop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_stop_id", nullable = false)
    private StopEntity destinationStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarded_stop_id")
    private StopEntity boardedStop;

    @Column(name = "journey_key", nullable = false, length = 120)
    private String journeyKey;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    public ConfirmedJourneyEntity() {
    }

    public ConfirmedJourneyEntity(PassengerEntity passenger,
                                  BusEntity selectedBus,
                                  StopEntity sourceStop,
                                  StopEntity destinationStop,
                                  StopEntity boardedStop,
                                  String journeyKey,
                                  LocalDateTime confirmedAt) {
        this.passenger = passenger;
        this.selectedBus = selectedBus;
        this.sourceStop = sourceStop;
        this.destinationStop = destinationStop;
        this.boardedStop = boardedStop;
        this.journeyKey = journeyKey;
        this.confirmedAt = confirmedAt;
    }

    public Long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(Long journeyId) {
        this.journeyId = journeyId;
    }

    public PassengerEntity getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerEntity passenger) {
        this.passenger = passenger;
    }

    public BusEntity getSelectedBus() {
        return selectedBus;
    }

    public void setSelectedBus(BusEntity selectedBus) {
        this.selectedBus = selectedBus;
    }

    public StopEntity getSourceStop() {
        return sourceStop;
    }

    public void setSourceStop(StopEntity sourceStop) {
        this.sourceStop = sourceStop;
    }

    public StopEntity getDestinationStop() {
        return destinationStop;
    }

    public void setDestinationStop(StopEntity destinationStop) {
        this.destinationStop = destinationStop;
    }

    public StopEntity getBoardedStop() {
        return boardedStop;
    }

    public void setBoardedStop(StopEntity boardedStop) {
        this.boardedStop = boardedStop;
    }

    public String getJourneyKey() {
        return journeyKey;
    }

    public void setJourneyKey(String journeyKey) {
        this.journeyKey = journeyKey;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
