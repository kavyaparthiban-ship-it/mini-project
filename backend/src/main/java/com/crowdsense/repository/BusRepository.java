package com.crowdsense.repository;

import com.crowdsense.entity.BusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<BusEntity, Long> {

    Optional<BusEntity> findByBusNumber(String busNumber);

    List<BusEntity> findByRoute_RouteId(Long routeId);

    @Query("SELECT DISTINCT b FROM BusEntity b JOIN FETCH b.route r LEFT JOIN FETCH b.currentStop " +
            "WHERE EXISTS (SELECT 1 FROM RouteStopEntity rsSrc WHERE rsSrc.route = r AND rsSrc.stop.stopId = :sourceId " +
            "AND EXISTS (SELECT 1 FROM RouteStopEntity rsDst WHERE rsDst.route = r AND rsDst.stop.stopId = :destinationId " +
            "AND rsSrc.stopSequence < rsDst.stopSequence))")
    List<BusEntity> findBusesServingDestination(@Param("sourceId") Long sourceId,
                                                @Param("destinationId") Long destinationId);
}
