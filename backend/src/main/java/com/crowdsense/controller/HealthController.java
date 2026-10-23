package com.crowdsense.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> map = new HashMap<>();
        map.put("service", "crowdsense-backend");
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            map.put("status", "UP");
            map.put("database", "UP");
            map.put("message", "CrowdSense API and database are available");
            return ResponseEntity.ok(map);
        } catch (DataAccessException ex) {
            logger.warn("Health check failed because the database is unavailable", ex);
            map.put("status", "DOWN");
            map.put("database", "DOWN");
            map.put("message", "Database unavailable. Start MySQL and verify DB_URL, DB_USERNAME, and DB_PASSWORD.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(map);
        }
    }
}
