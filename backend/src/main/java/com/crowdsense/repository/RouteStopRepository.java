package com.crowdsense.repository;

import com.crowdsense.entity.RouteStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStopEntity, Long> {

    List<RouteStopEntity> findByRoute_RouteIdOrderByStopSequenceAsc(Long routeId);

    @Query("SELECT rs FROM RouteStopEntity rs JOIN FETCH rs.stop WHERE rs.route.routeId = :routeId " +
            "ORDER BY rs.stopSequence ASC")
    List<RouteStopEntity> findStopsOnRoute(@Param("routeId") Long routeId);
}
