package com.crowdsense.repository;

import com.crowdsense.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StopRepository extends JpaRepository<StopEntity, Long> {

    Optional<StopEntity> findByStopName(String stopName);
}
