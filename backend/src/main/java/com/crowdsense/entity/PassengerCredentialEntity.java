package com.crowdsense.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "passenger_credential")
public class PassengerCredentialEntity {

    @Id
    @Column(name = "passenger_id")
    private Long passengerId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    protected PassengerCredentialEntity() {
    }

    public PassengerCredentialEntity(Long passengerId, String passwordHash) {
        this.passengerId = passengerId;
        this.passwordHash = passwordHash;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
