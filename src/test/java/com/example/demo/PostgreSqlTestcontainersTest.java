package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testcontainers - PostgreSQL Live Container Verification")
class PostgreSqlTestcontainersTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should connect to live PostgreSQL container and verify version")
    void shouldConnectToPostgresContainer() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");

        String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
        System.out.println(">>> Connected to: " + dbVersion);

        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result);
    }
}
