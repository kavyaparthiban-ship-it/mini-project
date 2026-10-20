-- =============================================================================
-- CrowdSense : Analytical and application queries
-- -----------------------------------------------------------------------------
-- Module : Database and Transport Data (Member 3)
-- File   : database/queries.sql
-- Purpose: Documents the important SQL used by the application and provides
--          ready-to-run verification queries for the demo.
-- =============================================================================

USE crowdsense;

-- ---------------------------------------------------------------------------
-- Q1. All buses with their route and latest simulated occupancy.
--     This mirrors GET /api/buses.
-- ---------------------------------------------------------------------------
SELECT b.bus_id,
       b.bus_number,
       r.route_code,
       r.route_name,
       r.transport_mode,
       b.capacity,
       o.occupancy_percent,
       o.passenger_count,
       o.recorded_at
FROM bus b
JOIN route r ON r.route_id = b.route_id
LEFT JOIN occupancy_record o
       ON o.bus_id = b.bus_id
      AND o.recorded_at = (SELECT MAX(o2.recorded_at)
                           FROM occupancy_record o2
                           WHERE o2.bus_id = b.bus_id)
ORDER BY b.bus_number;

-- ---------------------------------------------------------------------------
-- Q2. Buses that can serve a requested source -> destination pair.
--     A bus serves the pair when its route visits BOTH stops and the source
--     appears before the destination in the route sequence.
--     Replace :source and :destination with stop ids (or join on stop_name).
--     This mirrors GET /api/buses/search?source=..&destination=..
--     Demonstration values: source = 'Central Bus Terminal' (1),
--                           destination = 'Tech Park Gate' (4)
-- ---------------------------------------------------------------------------
SELECT b.bus_id,
       b.bus_number,
       r.route_code,
       rs_src.stop_sequence                AS source_sequence,
       rs_dst.stop_sequence                AS destination_sequence,
       (rs_dst.distance_from_start - rs_src.distance_from_start) AS trip_distance_km,
       o.occupancy_percent
FROM bus b
JOIN route r            ON r.route_id = b.route_id
JOIN route_stop rs_src  ON rs_src.route_id = r.route_id
JOIN route_stop rs_dst  ON rs_dst.route_id = r.route_id
JOIN stop s_src         ON s_src.stop_id = rs_src.stop_id
JOIN stop s_dst         ON s_dst.stop_id = rs_dst.stop_id
LEFT JOIN occupancy_record o
       ON o.bus_id = b.bus_id
      AND o.recorded_at = (SELECT MAX(o2.recorded_at)
                           FROM occupancy_record o2
                           WHERE o2.bus_id = b.bus_id)
WHERE s_src.stop_name = 'Central Bus Terminal'      -- :source
  AND s_dst.stop_name = 'Tech Park Gate'            -- :destination
  AND rs_src.stop_sequence < rs_dst.stop_sequence
ORDER BY o.occupancy_percent ASC;

-- ---------------------------------------------------------------------------
-- Q3. Latest occupancy per bus with a derived crowd category.
--     Category thresholds mirror the Java CrowdCategory enum:
--         Critical >= 85, High >= 65, Moderate >= 40, else Low.
-- ---------------------------------------------------------------------------
SELECT b.bus_number,
       o.occupancy_percent,
       CASE
           WHEN o.occupancy_percent >= 85 THEN 'Critical'
           WHEN o.occupancy_percent >= 65 THEN 'High'
           WHEN o.occupancy_percent >= 40 THEN 'Moderate'
           ELSE 'Low'
       END AS crowd_category
FROM bus b
JOIN occupancy_record o
       ON o.bus_id = b.bus_id
      AND o.recorded_at = (SELECT MAX(o2.recorded_at)
                           FROM occupancy_record o2
                           WHERE o2.bus_id = b.bus_id)
ORDER BY o.occupancy_percent DESC;

-- ---------------------------------------------------------------------------
-- Q4. Alternative buses for a crowded bus: same destination stop reachable,
--     with lower occupancy than the crowded bus.
--     Crowded bus = Bus 101 (id 1). Destination = Tech Park Gate (id 4).
-- ---------------------------------------------------------------------------
SELECT alt.bus_number            AS alternative_bus,
       alt_r.route_code          AS alternative_route,
       alt_o.occupancy_percent   AS alternative_occupancy,
       rs_alt.stop_id            AS boards_at_stop
FROM bus crowded
JOIN bus alt                 ON alt.bus_id <> crowded.bus_id
JOIN route alt_r             ON alt_r.route_id = alt.route_id
JOIN route_stop rs_alt       ON rs_alt.route_id = alt_r.route_id
JOIN route_stop rs_alt_dst   ON rs_alt_dst.route_id = alt_r.route_id
                            AND rs_alt_dst.stop_id = 4           -- destination
JOIN occupancy_record alt_o  ON alt_o.bus_id = alt.bus_id
                            AND alt_o.recorded_at = (SELECT MAX(o2.recorded_at)
                                                     FROM occupancy_record o2
                                                     WHERE o2.bus_id = alt.bus_id)
WHERE crowded.bus_id = 1
  AND rs_alt.stop_sequence < rs_alt_dst.stop_sequence
  AND alt_o.occupancy_percent < (SELECT o3.occupancy_percent
                                 FROM occupancy_record o3
                                 WHERE o3.bus_id = crowded.bus_id
                                 ORDER BY o3.recorded_at DESC LIMIT 1)
ORDER BY alt_o.occupancy_percent ASC;

-- ---------------------------------------------------------------------------
-- Q5. Stopping sequence of a route for the route-diagram view.
--     Example: Route R102 (route_id = 2).
-- ---------------------------------------------------------------------------
SELECT rs.stop_sequence,
       s.stop_name,
       s.stop_code,
       rs.distance_from_start
FROM route_stop rs
JOIN stop s ON s.stop_id = rs.stop_id
WHERE rs.route_id = 2
ORDER BY rs.stop_sequence;

-- ---------------------------------------------------------------------------
-- Q6. Passenger preference profile.
-- ---------------------------------------------------------------------------
SELECT p.passenger_name,
       pr.prefer_low_crowd,
       pr.prefer_short_wait,
       pr.prefer_short_travel,
       pr.prefer_short_walk,
       pr.max_walk_distance_km
FROM passenger p
JOIN passenger_preference pr ON pr.passenger_id = p.passenger_id
WHERE p.passenger_id = 1;

-- ---------------------------------------------------------------------------
-- Q7. Reward balance and full reward history for a passenger.
--     Mirrors GET /api/passengers/{id}/rewards and /rewards/history.
-- ---------------------------------------------------------------------------
SELECT p.passenger_name, p.reward_balance
FROM passenger p
WHERE p.passenger_id = 1;

SELECT rt.transaction_id,
       rt.journey_key,
       rt.points,
       rt.transaction_type,
       rt.description,
       rt.created_at
FROM reward_transaction rt
WHERE rt.passenger_id = 1
ORDER BY rt.created_at DESC;

-- ---------------------------------------------------------------------------
-- Q8. Duplicate-reward check. Returns 0 rows when the journey has not been
--     rewarded yet (the normal case before an award).
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS already_rewarded
FROM reward_transaction
WHERE passenger_id = 1
  AND journey_key = 'SEED-2026-10-19-001';

-- ---------------------------------------------------------------------------
-- Q9. Average occupancy per route (useful for crowd analysis reporting).
-- ---------------------------------------------------------------------------
SELECT r.route_code,
       r.route_name,
       ROUND(AVG(o.occupancy_percent), 2) AS avg_occupancy,
       COUNT(o.occupancy_id)              AS samples
FROM route r
JOIN bus b               ON b.route_id = r.route_id
LEFT JOIN occupancy_record o ON o.bus_id = b.bus_id
GROUP BY r.route_id, r.route_code, r.route_name
ORDER BY avg_occupancy DESC;
