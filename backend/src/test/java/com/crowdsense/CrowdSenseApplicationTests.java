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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
}
