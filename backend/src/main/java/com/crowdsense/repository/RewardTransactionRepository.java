package com.crowdsense.repository;

import com.crowdsense.entity.RewardTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransactionEntity, Long> {

    boolean existsByPassenger_PassengerIdAndJourneyKey(Long passengerId, String journeyKey);

    List<RewardTransactionEntity> findByPassenger_PassengerIdOrderByCreatedAtDesc(Long passengerId);
}
