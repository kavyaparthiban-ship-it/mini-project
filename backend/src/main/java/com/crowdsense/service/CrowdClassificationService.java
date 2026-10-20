package com.crowdsense.service;

import com.crowdsense.config.CrowdSenseProperties;
import com.crowdsense.entity.OccupancyEntity;
import com.crowdsense.model.CrowdCategory;
import com.crowdsense.model.Occupancy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CrowdClassificationService {

    private final CrowdSenseProperties properties;

    public CrowdClassificationService(CrowdSenseProperties properties) {
        this.properties = properties;
    }

    public Occupancy classify(OccupancyEntity entity) {
        if (entity == null) {
            return Occupancy.fromPercent(null, 1, true);
        }
        LocalDateTime now = LocalDateTime.now();
        boolean stale = Duration.between(entity.getRecordedAt(), now).toMinutes()
                > properties.getCrowd().getStaleAfterMinutes();
        // Never treat missing/stale as zero crowding - Occupancy.fromPercent already preserves null
        return Occupancy.fromPercent(entity.getOccupancyPercent(), getCapacity(entity), stale);
    }

    public Occupancy classifyWithCapacity(Double percent, int capacity, LocalDateTime recordedAt) {
        boolean stale = false;
        if (recordedAt != null) {
            stale = Duration.between(recordedAt, LocalDateTime.now()).toMinutes()
                    > properties.getCrowd().getStaleAfterMinutes();
        }
        return Occupancy.fromPercent(percent, capacity, stale);
    }

    public CrowdCategory classifyCategory(Double percent) {
        return CrowdCategory.classify(percent,
                properties.getCrowd().getThresholdModerate(),
                properties.getCrowd().getThresholdHigh(),
                properties.getCrowd().getThresholdCritical());
    }

    private int getCapacity(OccupancyEntity entity) {
        if (entity.getBus() != null && entity.getBus().getCapacity() != null) {
            return entity.getBus().getCapacity();
        }
        return 50;
    }

    public int getCriticalThreshold() {
        return (int) properties.getCrowd().getThresholdCritical();
    }

    public int getHighThreshold() {
        return (int) properties.getCrowd().getThresholdHigh();
    }

    public int getModerateThreshold() {
        return (int) properties.getCrowd().getThresholdModerate();
    }
}
