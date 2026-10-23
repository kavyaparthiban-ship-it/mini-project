package com.crowdsense.repository;

import com.crowdsense.entity.PassengerCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerCredentialRepository extends JpaRepository<PassengerCredentialEntity, Long> {
}
