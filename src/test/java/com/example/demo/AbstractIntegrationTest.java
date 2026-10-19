package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    static {
        try {
            log.info("Starting PostgreSQL Testcontainer...");
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
            postgres.start();
            log.info("PostgreSQL Testcontainer started successfully at jdbc url: {}", postgres.getJdbcUrl());
        } catch (Exception e) {
            log.error("Failed to start PostgreSQL Testcontainer", e);
            throw new RuntimeException("Could not initialize PostgreSQL container", e);
        }
    }
}
