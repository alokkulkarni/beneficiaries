package com.alok.payment.beneficiaries.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class BeneficiariesApplicationIntegrationTest {

	@Container
	@ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("beneficiaries_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.db");

	@Test
	void contextLoads() {
	}

}
