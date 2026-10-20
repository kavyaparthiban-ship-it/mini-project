package com.crowdsense.model;

import java.util.Objects;

/**
 * A registered CrowdSense user and the owner of a reward balance.
 */
public class Passenger {

    private final Long id;
    private final String name;
    private final String email;
    private int rewardBalance;

    public Passenger(Long id, String name, String email, int rewardBalance) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Passenger name is required");
        this.email = Objects.requireNonNull(email, "Passenger email is required");
        if (rewardBalance < 0) {
            throw new IllegalArgumentException("Reward balance must not be negative");
        }
        this.rewardBalance = rewardBalance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getRewardBalance() {
        return rewardBalance;
    }

    /**
     * Adds points to the balance.
     * @throws IllegalArgumentException when the amount is not positive
     */
    public void credit(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Reward points must be positive");
        }
        this.rewardBalance += points;
    }

    /** Removes points from the balance, refusing to go below zero. */
    public void debit(int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Redeemed points must be positive");
        }
        if (points > rewardBalance) {
            throw new IllegalStateException("Insufficient reward balance");
        }
        this.rewardBalance -= points;
    }

    @Override
    public String toString() {
        return name + " <" + email + "> (" + rewardBalance + " pts)";
    }
}
