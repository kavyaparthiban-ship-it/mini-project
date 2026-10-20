package com.crowdsense.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds crowdsense.* configuration properties into a POJO. This makes them easy
 * to inject into services and to document the scoring/reward rules.
 */
@Configuration
@ConfigurationProperties(prefix = "crowdsense")
public class CrowdSenseProperties {

    private Cors cors = new Cors();
    private Crowd crowd = new Crowd();
    private Recommendation recommendation = new Recommendation();
    private Waiting waiting = new Waiting();
    private Travel travel = new Travel();
    private Reward reward = new Reward();

    public static class Cors {
        private String allowedOrigins;

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Crowd {
        private double thresholdModerate = 40.0;
        private double thresholdHigh = 65.0;
        private double thresholdCritical = 85.0;
        private int staleAfterMinutes = 720;

        public double getThresholdModerate() {
            return thresholdModerate;
        }

        public void setThresholdModerate(double thresholdModerate) {
            this.thresholdModerate = thresholdModerate;
        }

        public double getThresholdHigh() {
            return thresholdHigh;
        }

        public void setThresholdHigh(double thresholdHigh) {
            this.thresholdHigh = thresholdHigh;
        }

        public double getThresholdCritical() {
            return thresholdCritical;
        }

        public void setThresholdCritical(double thresholdCritical) {
            this.thresholdCritical = thresholdCritical;
        }

        public int getStaleAfterMinutes() {
            return staleAfterMinutes;
        }

        public void setStaleAfterMinutes(int staleAfterMinutes) {
            this.staleAfterMinutes = staleAfterMinutes;
        }
    }

    public static class Recommendation {
        private double weightCrowd = 0.40;
        private double weightWait = 0.20;
        private double weightTravel = 0.20;
        private double weightWalk = 0.20;

        public double getWeightCrowd() {
            return weightCrowd;
        }

        public void setWeightCrowd(double weightCrowd) {
            this.weightCrowd = weightCrowd;
        }

        public double getWeightWait() {
            return weightWait;
        }

        public void setWeightWait(double weightWait) {
            this.weightWait = weightWait;
        }

        public double getWeightTravel() {
            return weightTravel;
        }

        public void setWeightTravel(double weightTravel) {
            this.weightTravel = weightTravel;
        }

        public double getWeightWalk() {
            return weightWalk;
        }

        public void setWeightWalk(double weightWalk) {
            this.weightWalk = weightWalk;
        }
    }

    public static class Waiting {
        private int avgHeadwayMinutes = 12;

        public int getAvgHeadwayMinutes() {
            return avgHeadwayMinutes;
        }

        public void setAvgHeadwayMinutes(int avgHeadwayMinutes) {
            this.avgHeadwayMinutes = avgHeadwayMinutes;
        }
    }

    public static class Travel {
        private double avgSpeedKmph = 20.0;

        public double getAvgSpeedKmph() {
            return avgSpeedKmph;
        }

        public void setAvgSpeedKmph(double avgSpeedKmph) {
            this.avgSpeedKmph = avgSpeedKmph;
        }
    }

    public static class Reward {
        private int points = 5;
        private double minCrowdImprovementPercent = 15.0;

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public double getMinCrowdImprovementPercent() {
            return minCrowdImprovementPercent;
        }

        public void setMinCrowdImprovementPercent(double minCrowdImprovementPercent) {
            this.minCrowdImprovementPercent = minCrowdImprovementPercent;
        }
    }

    public Cors getCors() {
        return cors;
    }

    public Crowd getCrowd() {
        return crowd;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Travel getTravel() {
        return travel;
    }

    public Reward getReward() {
        return reward;
    }
}
