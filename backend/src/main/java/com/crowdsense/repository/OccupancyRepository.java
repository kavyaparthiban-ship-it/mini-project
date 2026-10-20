package com.crowdsense.repository;

import com.crowdsense.entity.OccupancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OccupancyRepository extends JpaRepository<OccupancyEntity, Long> {

    @Query("SELECT o FROM OccupancyEntity o WHERE o.bus.busId = :busId " +
            "ORDER BY o.recordedAt DESC")
    java.util.List<OccupancyEntity> findRecentByBus(@Param("busId") Long busId);

    default Optional<OccupancyEntity> findLatestByBus(Long busId) {
        java.util.List<OccupancyEntity> list = findRecentByBus(busId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
