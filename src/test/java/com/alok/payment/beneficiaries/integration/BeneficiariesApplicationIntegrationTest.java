package com.alok.payment.beneficiaries.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class BeneficiariesApplicationIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("testcontainers-postgres:16-alpine")
			.withDatabaseName("beneficiaries_test")
			.withUsername("test")
			.withPassword("test");

	@Test
	void contextLoads() {
	}

}
