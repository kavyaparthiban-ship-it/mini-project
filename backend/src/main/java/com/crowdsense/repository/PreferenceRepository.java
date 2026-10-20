package com.crowdsense.repository;

import com.crowdsense.entity.PreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {

    Optional<PreferenceEntity> findByPassenger_PassengerId(Long passengerId);
}
