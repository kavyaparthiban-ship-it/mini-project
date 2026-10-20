-- =============================================================================
-- CrowdSense : Simulated transport seed data
-- -----------------------------------------------------------------------------
-- Module  : Database and Transport Data (Member 3)
-- File    : database/data.sql
-- Purpose : Loads realistic *simulated* transport data used by the demo.
-- NOTE    : This is SAMPLE/SIMULATED data for a college mini-project.
--           It does NOT come from any real transport operator or live feed.
--           It includes the required demo scenario:
--               Bus 101 -> ~94% crowd (Critical)
--               Bus 102 -> ~42% crowd (Moderate) recommended alternative
--               Bus 102 waiting time ~6 minutes, reward 5 virtual vouchers.
-- =============================================================================

USE crowdsense;

-- ---------------------------------------------------------------------------
-- Stops
-- ---------------------------------------------------------------------------
INSERT INTO stop (stop_id, stop_name, stop_code, latitude, longitude) VALUES
 (1,  'Central Bus Terminal', 'CBT',  12.971599, 77.594566),
 (2,  'Market Junction',      'MKJ',  12.975200, 77.600100),
 (3,  'City Hospital',        'CTH',  12.980300, 77.605500),
 (4,  'Tech Park Gate',       'TPG',  12.986100, 77.612300),
 (5,  'University Circle',    'UNC',  12.991000, 77.618900),
 (6,  'Riverside Colony',     'RVC',  12.966100, 77.588200),
 (7,  'Old Town Square',      'OTS',  12.963900, 77.583100),
 (8,  'Airport Road Stop',    'ARS',  12.995600, 77.626400),
 (9,  'Metro Interchange',    'MTI',  12.978900, 77.601200),
 (10, 'Green Lake Park',      'GLP',  12.969400, 77.591800);

-- ---------------------------------------------------------------------------
-- Routes
-- ---------------------------------------------------------------------------
INSERT INTO route (route_id, route_code, route_name, transport_mode) VALUES
 (1, 'R101', 'Central Terminal - University Circle',   'BUS'),
 (2, 'R102', 'Central Terminal - Tech Park Gate',      'BUS'),
 (3, 'R103', 'Old Town Square - Airport Road Stop',    'BUS'),
 (4, 'R104', 'Riverside Colony - Green Lake Park',     'BUS'),
 (5, 'R105', 'Central Terminal - Green Lake Park',     'BUS'),
 (6, 'M201', 'Metro Interchange - Airport Road Stop',  'METRO');

-- ---------------------------------------------------------------------------
-- Route-stop ordering.  distance_from_start in kilometres (simulated).
-- ---------------------------------------------------------------------------
INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES
 -- Route 1 : CBT -> MKJ -> CTH -> TPG -> UNC
 (1, 1, 1, 0.00), (1, 2, 2, 0.90), (1, 3, 3, 1.90), (1, 4, 4, 3.10), (1, 5, 5, 4.40),
 -- Route 2 : CBT -> MKJ -> CTH -> TPG
 (2, 1, 1, 0.00), (2, 2, 2, 0.80), (2, 3, 3, 1.80), (2, 4, 4, 3.00),
 -- Route 3 : OTS -> RVC -> CBT -> MKJ -> CTH -> TPG -> ARS
 (3, 7, 1, 0.00), (3, 6, 2, 0.70), (3, 1, 3, 1.60), (3, 2, 4, 2.50),
 (3, 3, 5, 3.50), (3, 4, 6, 4.70), (3, 8, 7, 6.20),
 -- Route 4 : RVC -> GLP -> CBT
 (4, 6, 1, 0.00), (4, 10, 2, 0.80), (4, 1, 3, 1.90),
 -- Route 5 : CBT -> GLP -> RVC -> OTS
 (5, 1, 1, 0.00), (5, 10, 2, 0.80), (5, 6, 3, 1.70), (5, 7, 4, 2.40),
 -- Route 6 (METRO) : MTI -> TPG -> ARS
 (6, 9, 1, 0.00), (6, 4, 2, 1.50), (6, 8, 3, 3.10);

-- ---------------------------------------------------------------------------
-- Buses.  Required demo scenario:
--   Bus 101 serves CBT..TPG (route 1 + route 2 overlap) and is Critical.
--   Bus 102 serves route 2 and is the recommended alternative.
-- ---------------------------------------------------------------------------
INSERT INTO bus (bus_id, bus_number, route_id, capacity, current_stop_id) VALUES
 (1, '101', 1, 50, 3),
 (2, '102', 2, 50, 2),
 (3, '103', 3, 60, 6),
 (4, '104', 4, 45, 10),
 (5, '105', 5, 50, 1),
 (6, '106', 1, 50, 2),
 (7, 'M201', 6, 120, 9);

-- ---------------------------------------------------------------------------
-- Passengers
-- ---------------------------------------------------------------------------
INSERT INTO passenger (passenger_id, passenger_name, email, phone, reward_balance) VALUES
 (1, 'Ananya Sharma', 'ananya.sharma@example.com', '9000000001', 15),
 (2, 'Rahul Verma',   'rahul.verma@example.com',   '9000000002', 0),
 (3, 'Priya Nair',    'priya.nair@example.com',    '9000000003', 5);

-- ---------------------------------------------------------------------------
-- Passenger preferences
-- ---------------------------------------------------------------------------
INSERT INTO passenger_preference
 (passenger_id, prefer_low_crowd, prefer_short_wait, prefer_short_travel, prefer_short_walk, max_walk_distance_km) VALUES
 (1, 9, 7, 5, 4, 1.50),
 (2, 6, 6, 6, 6, 2.00),
 (3, 8, 5, 7, 6, 1.20);

-- ---------------------------------------------------------------------------
-- Occupancy records (simulated "current" tick = 2026-10-20 09:00:00).
-- Bus 101 on route 1 at City Hospital  -> 47/50 = 94.00 %  (Critical)
-- Bus 102 on route 2 at Market Junction -> 21/50 = 42.00 %  (Moderate)
-- ---------------------------------------------------------------------------
INSERT INTO occupancy_record
 (bus_id, stop_id, occupancy_percent, passenger_count, recorded_at) VALUES
 (1, 3, 94.00, 47, '2026-10-20 09:00:00'),   -- Bus 101 : Critical
 (6, 2, 88.00, 44, '2026-10-20 09:00:00'),   -- Bus 106 : Critical
 (2, 2, 42.00, 21, '2026-10-20 09:00:00'),   -- Bus 102 : Moderate  (recommended)
 (3, 6, 30.00, 18, '2026-10-20 09:00:00'),   -- Bus 103 : Low
 (4, 10, 55.00, 25, '2026-10-20 09:00:00'),  -- Bus 104 : High
 (5, 1, 26.00, 13, '2026-10-20 09:00:00'),   -- Bus 105 : Low
 (7, 9, 68.00, 82, '2026-10-20 09:00:00');   -- Metro M201 : High

-- ---------------------------------------------------------------------------
-- Seed reward history for passenger 1 so the dashboard has data to show.
-- ---------------------------------------------------------------------------
INSERT INTO reward_transaction
 (passenger_id, journey_key, points, transaction_type, description, created_at) VALUES
 (1, 'SEED-2026-10-19-001', 5, 'EARNED', 'Reward for choosing less-crowded Bus 102', '2026-10-19 08:45:00'),
 (1, 'SEED-2026-10-18-001', 5, 'EARNED', 'Reward for choosing less-crowded Bus 103', '2026-10-18 18:20:00'),
 (1, 'SEED-2026-10-17-001', 5, 'EARNED', 'Reward for choosing less-crowded Bus 105', '2026-10-17 09:10:00');

INSERT INTO confirmed_journey
 (passenger_id, selected_bus_id, source_stop_id, destination_stop_id, boarded_stop_id, journey_key, confirmed_at) VALUES
 (1, 2, 1, 4, 2, 'SEED-2026-10-19-001', '2026-10-19 08:45:00'),
 (1, 3, 7, 4, 6, 'SEED-2026-10-18-001', '2026-10-18 18:20:00'),
 (1, 5, 1, 7, 1, 'SEED-2026-10-17-001', '2026-10-17 09:10:00');
