package com.alok.payment.beneficiaries.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class BeneficiariesApplicationIntegrationTest {

	@Container
	@ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine")
                    .asCompatibleSubstituteFor("postgres"))
            .withImagePullPolicy(PullPolicy.defaultPolicy())
            .withDatabaseName("beneficiaries_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.db");

	@SuppressWarnings("resource")
	@Container
	static final GenericContainer<?> redis = new GenericContainer<>(
			DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/redis:7-alpine")
					.asCompatibleSubstituteFor("redis"))
			.withImagePullPolicy(PullPolicy.defaultPolicy())
			.withExposedPorts(6379);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
	}

	@Test
	void contextLoads() {
	}

}
