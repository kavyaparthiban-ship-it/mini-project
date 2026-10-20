package com.crowdsense.repository;

import com.crowdsense.entity.ConfirmedJourneyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmedJourneyRepository extends JpaRepository<ConfirmedJourneyEntity, Long> {

    boolean existsByPassenger_PassengerIdAndJourneyKey(Long passengerId, String journeyKey);
}
