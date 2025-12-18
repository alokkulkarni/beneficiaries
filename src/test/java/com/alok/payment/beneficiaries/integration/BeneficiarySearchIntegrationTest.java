package com.alok.payment.beneficiaries.integration;

import com.alok.payment.beneficiaries.dto.BeneficiarySearchCriteria;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Beneficiary Search Integration Tests")
class BeneficiarySearchIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
            .withImagePullPolicy(PullPolicy.defaultPolicy())
            .withDatabaseName("beneficiaries_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.db");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/redis:7-alpine").asCompatibleSubstituteFor("redis"))
            .withImagePullPolicy(PullPolicy.defaultPolicy())
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    private void insertBeneficiary(String customerId, String name, String type, String status, String bankCode, LocalDateTime createdAt) {
        Beneficiary b = new Beneficiary();
        b.setCustomerId(customerId);
        b.setAccountNumber("ACC_" + customerId);
        b.setBeneficiaryName(name);
        b.setBeneficiaryAccountNumber("ACCT_" + name.replaceAll("\\s+", "").toUpperCase());
        b.setBeneficiaryBankCode(bankCode);
        b.setBeneficiaryBankName("Bank " + bankCode);
        b.setBeneficiaryType(type);
        b.setStatus(status);
        b.setCreatedAt(createdAt);
        b.setUpdatedAt(createdAt);
        beneficiaryRepository.save(b);
    }

    @BeforeEach
    void seedSearchData() {
        // Start each test with a clean slate to avoid duplicate key violations across runs
        beneficiaryRepository.deleteAll();

        // Use a dedicated customer to avoid coupling with init.db
        String customer = "CUST_SEARCH_IT";
        // Insert a diverse set
        insertBeneficiary(customer, "John One", "DOMESTIC", "ACTIVE", "BANKA", LocalDateTime.now().minusDays(3));
        insertBeneficiary(customer, "Alice Two", "INTERNATIONAL", "ACTIVE", "BANKB", LocalDateTime.now().minusDays(2));
        insertBeneficiary(customer, "John Three", "DOMESTIC", "INACTIVE", "BANKA", LocalDateTime.now().minusDays(1));
        insertBeneficiary(customer, "Bob Four", "DOMESTIC", "ACTIVE", "BANKC", LocalDateTime.now());
    }

    @Test
    @DisplayName("Search by customer and name contains with pagination and sort")
    void searchByNameWithPagingAndSort() throws Exception {
        BeneficiarySearchCriteria criteria = new BeneficiarySearchCriteria();
        criteria.setCustomerId("CUST_SEARCH_IT");
        criteria.setBeneficiaryName("john"); // case-insensitive contains
        criteria.setPage(0);
        criteria.setSize(2);
        criteria.setSortBy("beneficiaryName");
        criteria.setSortDirection("ASC");

        mockMvc.perform(post("/api/v1/beneficiaries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content[0].beneficiaryName", containsStringIgnoringCase("John")));
    }

    @Test
    @DisplayName("Search with type and status filters")
    void searchWithTypeAndStatus() throws Exception {
        BeneficiarySearchCriteria criteria = new BeneficiarySearchCriteria();
        criteria.setCustomerId("CUST_SEARCH_IT");
        criteria.setBeneficiaryType("DOMESTIC");
        criteria.setStatus("ACTIVE");
        criteria.setPage(0);
        criteria.setSize(10);

        mockMvc.perform(post("/api/v1/beneficiaries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].beneficiaryType", everyItem(equalTo("DOMESTIC"))))
                .andExpect(jsonPath("$.content[*].status", everyItem(equalTo("ACTIVE"))));
    }

    @Test
    @DisplayName("Search with bank code and date range")
    void searchWithBankCodeAndDateRange() throws Exception {
        LocalDateTime after = LocalDateTime.now().minusDays(2);
        LocalDateTime before = LocalDateTime.now().plusDays(1);

        BeneficiarySearchCriteria criteria = new BeneficiarySearchCriteria();
        criteria.setCustomerId("CUST_SEARCH_IT");
        criteria.setBeneficiaryBankCode("BANKC");
        criteria.setCreatedAfter(after);
        criteria.setCreatedBefore(before);
        criteria.setPage(0);
        criteria.setSize(5);

        mockMvc.perform(post("/api/v1/beneficiaries/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].beneficiaryBankCode", equalTo("BANKC")));
    }
}
