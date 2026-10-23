package com.crowdsense;

import com.crowdsense.entity.BusEntity;
import com.crowdsense.repository.BusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CrowdSenseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void healthCheckReportsDatabaseAvailability() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"));
    }

    @Test
    void allowsConfiguredFrontendOriginToCallApi() throws Exception {
        mockMvc.perform(options("/api/recommendations")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5500"));
    }

    @Test
    void rejectsUnconfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/recommendations")
                        .header("Origin", "http://localhost:9999")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void findsBusWhenDestinationComesBeforeSourceOnStoredRoute() {
        jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                9001L, "Market Junction", "MKJ-TEST");
        jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                9002L, "City Hospital", "CTH-TEST");
        jdbcTemplate.update(
                "INSERT INTO route (route_id, route_code, route_name, transport_mode) VALUES (?, ?, ?, ?)",
                9001L, "REVERSE-TEST", "Reverse direction test", "BUS");
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9001L, 9001L, 2, 0.9);
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9001L, 9002L, 3, 1.9);
        jdbcTemplate.update(
                "INSERT INTO bus (bus_number, route_id, capacity) VALUES (?, ?, ?)",
                "REVERSE-TEST", 9001L, 50);

        List<BusEntity> matches = busRepository.findBusesServingDestination(
                9002L, 9001L);

        assertEquals(1, matches.size());
        assertEquals("REVERSE-TEST", matches.get(0).getBusNumber());
    }

    @Test
    @Transactional
    void recommendsBusToMetroTransferWhenNoDirectRouteExists() throws Exception {
        jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                9101L, "Transfer Test Source", "TTS");
        jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                9102L, "Transfer Test Hub", "TTH");
        jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                9103L, "Transfer Test Destination", "TTD");
        jdbcTemplate.update(
                "INSERT INTO route (route_id, route_code, route_name, transport_mode) VALUES (?, ?, ?, ?)",
                9101L, "TRANSFER-BUS", "Transfer Test Bus", "BUS");
        jdbcTemplate.update(
                "INSERT INTO route (route_id, route_code, route_name, transport_mode) VALUES (?, ?, ?, ?)",
                9102L, "TRANSFER-METRO", "Transfer Test Metro", "METRO");
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9101L, 9101L, 1, 0.0);
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9101L, 9102L, 2, 1.0);
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9102L, 9102L, 1, 0.0);
        jdbcTemplate.update(
                "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                9102L, 9103L, 2, 2.0);
        jdbcTemplate.update(
                "INSERT INTO bus (bus_id, bus_number, route_id, capacity) VALUES (?, ?, ?, ?)",
                9101L, "TRANSFER-BUS-1", 9101L, 50);
        jdbcTemplate.update(
                "INSERT INTO bus (bus_id, bus_number, route_id, capacity) VALUES (?, ?, ?, ?)",
                9102L, "TRANSFER-METRO-1", 9102L, 100);

        mockMvc.perform(post("/api/recommendations")
                        .contentType("application/json")
                        .content("""
                                {"source":"Transfer Test Source area","destination":"transfer test destination"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.sourceStopName").value("Transfer Test Source"))
                .andExpect(jsonPath("$.recommended.itinerary.length()").value(2))
                .andExpect(jsonPath("$.recommended.itinerary[0].transportMode").value("BUS"))
                .andExpect(jsonPath("$.recommended.itinerary[0].toStopName").value("Transfer Test Hub"))
                .andExpect(jsonPath("$.recommended.itinerary[1].transportMode").value("METRO"))
                .andExpect(jsonPath("$.recommended.itinerary[1].toStopName").value("Transfer Test Destination"));
    }

    @Test
    @Transactional
    void recommendsMultipleTransfersForLongerJourneys() throws Exception {
        for (long stopId = 9201; stopId <= 9204; stopId++) {
            jdbcTemplate.update("INSERT INTO stop (stop_id, stop_name, stop_code) VALUES (?, ?, ?)",
                    stopId, "Multi Transfer Stop " + stopId, "MTS-" + stopId);
        }
        for (int routeIndex = 0; routeIndex < 3; routeIndex++) {
            long routeId = 9201L + routeIndex;
            jdbcTemplate.update(
                    "INSERT INTO route (route_id, route_code, route_name, transport_mode) VALUES (?, ?, ?, ?)",
                    routeId, "MULTI-" + routeId, "Multi transfer route " + routeId, "BUS");
            jdbcTemplate.update(
                    "INSERT INTO bus (bus_id, bus_number, route_id, capacity) VALUES (?, ?, ?, ?)",
                    routeId, "MULTI-BUS-" + routeId, routeId, 50);
            jdbcTemplate.update(
                    "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                    routeId, 9201L + routeIndex, 1, 0.0);
            jdbcTemplate.update(
                    "INSERT INTO route_stop (route_id, stop_id, stop_sequence, distance_from_start) VALUES (?, ?, ?, ?)",
                    routeId, 9202L + routeIndex, 2, 2.0);
        }

        mockMvc.perform(post("/api/recommendations")
                        .contentType("application/json")
                        .content("""
                                {"source":"Multi Transfer Stop 9201","destination":"Multi Transfer Stop 9204"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommended.itinerary.length()").value(3))
                .andExpect(jsonPath("$.recommended.itinerary[0].toStopName").value("Multi Transfer Stop 9202"))
                .andExpect(jsonPath("$.recommended.itinerary[1].toStopName").value("Multi Transfer Stop 9203"))
                .andExpect(jsonPath("$.recommended.itinerary[2].toStopName").value("Multi Transfer Stop 9204"));
    }
}
