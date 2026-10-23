-- Safe to run against an existing CrowdSense database; no user data is removed.
USE crowdsense;

CREATE TABLE IF NOT EXISTS passenger_credential (
    passenger_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (passenger_id),
    CONSTRAINT fk_credential_passenger FOREIGN KEY (passenger_id)
        REFERENCES passenger (passenger_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
