-- =============================================================================
-- CrowdSense : Intelligent Crowd Prediction and Personalized Multi-Modal
--              Travel Recommendation System
-- -----------------------------------------------------------------------------
-- Module      : Database and Transport Data (Member 3)
-- File        : database/schema.sql
-- Purpose     : Creates a normalized relational schema for CrowdSense.
-- Database    : MySQL 8.x
-- NOTE        : All transport data used by this project is SIMULATED data for a
--               college OOPS mini-project. It is NOT live transport data.
-- =============================================================================

DROP DATABASE IF EXISTS crowdsense;
CREATE DATABASE crowdsense
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE crowdsense;

-- -----------------------------------------------------------------------------
-- 1. stop : A physical boarding/alighting point.
-- -----------------------------------------------------------------------------
CREATE TABLE stop (
    stop_id        BIGINT       NOT NULL AUTO_INCREMENT,
    stop_name      VARCHAR(120) NOT NULL,
    stop_code      VARCHAR(20)  NOT NULL,
    latitude       DECIMAL(9,6) NULL,
    longitude      DECIMAL(9,6) NULL,
    PRIMARY KEY (stop_id),
    UNIQUE KEY uq_stop_name (stop_name),
    UNIQUE KEY uq_stop_code (stop_code)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 2. route : An ordered path served by one or more buses.
-- -----------------------------------------------------------------------------
CREATE TABLE route (
    route_id        BIGINT       NOT NULL AUTO_INCREMENT,
    route_code      VARCHAR(20)  NOT NULL,
    route_name      VARCHAR(150) NOT NULL,
    transport_mode  VARCHAR(30)  NOT NULL,      -- BUS, METRO (simulated)
    PRIMARY KEY (route_id),
    UNIQUE KEY uq_route_code (route_code)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 3. route_stop : Ordered many-to-many link between routes and stops.
--    stop_sequence defines the travel order along the route.
-- -----------------------------------------------------------------------------
CREATE TABLE route_stop (
    route_stop_id       BIGINT      NOT NULL AUTO_INCREMENT,
    route_id            BIGINT      NOT NULL,
    stop_id             BIGINT      NOT NULL,
    stop_sequence       INT         NOT NULL,
    distance_from_start DECIMAL(6,2) NOT NULL,  -- kilometres from route origin
    PRIMARY KEY (route_stop_id),
    UNIQUE KEY uq_route_stop_seq (route_id, stop_sequence),
    UNIQUE KEY uq_route_stop (route_id, stop_id),
    KEY idx_route_stop_route (route_id),
    KEY idx_route_stop_stop (stop_id),
    CONSTRAINT fk_rs_route FOREIGN KEY (route_id) REFERENCES route (route_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rs_stop FOREIGN KEY (stop_id) REFERENCES stop (stop_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 4. bus : A vehicle that operates a route.
-- -----------------------------------------------------------------------------
CREATE TABLE bus (
    bus_id          BIGINT       NOT NULL AUTO_INCREMENT,
    bus_number      VARCHAR(20)  NOT NULL,
    route_id        BIGINT       NOT NULL,
    capacity        INT          NOT NULL,      -- maximum seated + standing
    current_stop_id BIGINT       NULL,          -- last reported stop (simulated)
    PRIMARY KEY (bus_id),
    UNIQUE KEY uq_bus_number (bus_number),
    KEY idx_bus_route (route_id),
    KEY idx_bus_current_stop (current_stop_id),
    CONSTRAINT fk_bus_route FOREIGN KEY (route_id) REFERENCES route (route_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bus_current_stop FOREIGN KEY (current_stop_id)
        REFERENCES stop (stop_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_bus_capacity CHECK (capacity > 0)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 5. passenger : A registered user of CrowdSense.
-- -----------------------------------------------------------------------------
CREATE TABLE passenger (
    passenger_id   BIGINT       NOT NULL AUTO_INCREMENT,
    passenger_name VARCHAR(120) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    phone          VARCHAR(20)  NULL,
    reward_balance INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (passenger_id),
    UNIQUE KEY uq_passenger_email (email),
    CONSTRAINT chk_passenger_balance CHECK (reward_balance >= 0)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 6. passenger_credential : Salted password hashes for registered accounts.
-- -----------------------------------------------------------------------------
CREATE TABLE passenger_credential (
    passenger_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (passenger_id),
    CONSTRAINT fk_credential_passenger FOREIGN KEY (passenger_id)
        REFERENCES passenger (passenger_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 7. passenger_preference : Weight the passenger assigns to each factor.
--    All weights are stored as non-negative integers 0..9 for easy explanation.
-- -----------------------------------------------------------------------------
CREATE TABLE passenger_preference (
    preference_id       BIGINT      NOT NULL AUTO_INCREMENT,
    passenger_id        BIGINT      NOT NULL,
    prefer_low_crowd    INT         NOT NULL DEFAULT 5,
    prefer_short_wait    INT         NOT NULL DEFAULT 5,
    prefer_short_travel INT         NOT NULL DEFAULT 5,
    prefer_short_walk   INT         NOT NULL DEFAULT 5,
    max_walk_distance_km DECIMAL(5,2) NOT NULL DEFAULT 1.50,
    PRIMARY KEY (preference_id),
    UNIQUE KEY uq_pref_passenger (passenger_id),
    CONSTRAINT fk_pref_passenger FOREIGN KEY (passenger_id)
        REFERENCES passenger (passenger_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 8. occupancy_record : Simulated crowd observation for a bus at a stop.
--    recorded_at uses a fixed simulated tick so the demo is reproducible.
-- -----------------------------------------------------------------------------
CREATE TABLE occupancy_record (
    occupancy_id     BIGINT       NOT NULL AUTO_INCREMENT,
    bus_id           BIGINT       NOT NULL,
    stop_id          BIGINT       NOT NULL,
    occupancy_percent DECIMAL(5,2) NOT NULL,
    passenger_count  INT          NOT NULL,
    recorded_at      DATETIME     NOT NULL,
    PRIMARY KEY (occupancy_id),
    KEY idx_occ_bus (bus_id),
    KEY idx_occ_stop (stop_id),
    KEY idx_occ_bus_time (bus_id, recorded_at),
    CONSTRAINT fk_occ_bus FOREIGN KEY (bus_id) REFERENCES bus (bus_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_occ_stop FOREIGN KEY (stop_id) REFERENCES stop (stop_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_occ_percent CHECK (occupancy_percent >= 0 AND occupancy_percent <= 100)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 9. reward_transaction : Audit trail of every reward awarded or redeemed.
--    journey_key is unique per journey -> prevents duplicate rewards.
-- -----------------------------------------------------------------------------
CREATE TABLE reward_transaction (
    transaction_id   BIGINT       NOT NULL AUTO_INCREMENT,
    passenger_id     BIGINT       NOT NULL,
    journey_key      VARCHAR(120) NOT NULL,
    points           INT          NOT NULL,
    transaction_type VARCHAR(20)  NOT NULL,     -- EARNED, REDEEMED
    description      VARCHAR(200) NOT NULL,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (transaction_id),
    UNIQUE KEY uq_reward_journey (passenger_id, journey_key),
    KEY idx_reward_passenger (passenger_id),
    CONSTRAINT fk_reward_passenger FOREIGN KEY (passenger_id)
        REFERENCES passenger (passenger_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 10. confirm_journey : Records a passenger confirmed (simulated) journey.
--    Keeping this separate from reward_transaction allows a journey to be
--    recorded even when it does not qualify for a reward.
-- -----------------------------------------------------------------------------
CREATE TABLE confirmed_journey (
    journey_id        BIGINT       NOT NULL AUTO_INCREMENT,
    passenger_id      BIGINT       NOT NULL,
    selected_bus_id   BIGINT       NOT NULL,
    source_stop_id    BIGINT       NOT NULL,
    destination_stop_id BIGINT     NOT NULL,
    boarded_stop_id   BIGINT       NULL,       -- alternative stop when applicable
    journey_key       VARCHAR(120) NOT NULL,
    confirmed_at      DATETIME     NOT NULL,
    PRIMARY KEY (journey_id),
    UNIQUE KEY uq_confirmed_journey (passenger_id, journey_key),
    KEY idx_cj_passenger (passenger_id),
    KEY idx_cj_bus (selected_bus_id),
    CONSTRAINT fk_cj_passenger FOREIGN KEY (passenger_id)
        REFERENCES passenger (passenger_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cj_bus FOREIGN KEY (selected_bus_id) REFERENCES bus (bus_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cj_source FOREIGN KEY (source_stop_id)
        REFERENCES stop (stop_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cj_destination FOREIGN KEY (destination_stop_id)
        REFERENCES stop (stop_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cj_boarded FOREIGN KEY (boarded_stop_id)
        REFERENCES stop (stop_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;
