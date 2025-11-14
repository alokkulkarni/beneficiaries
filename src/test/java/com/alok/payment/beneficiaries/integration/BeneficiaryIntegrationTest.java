package com.alok.payment.beneficiaries.integration;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Beneficiary Integration Tests")
class BeneficiaryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("beneficiaries_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.db");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/redis:7-alpine").asCompatibleSubstituteFor("redis"))
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
    
    @BeforeEach
    void setUp() {
        // Don't delete all - init.db provides test data
        // Only clean up data created by dynamic tests if needed
    }
    
    private BeneficiaryRequest createRequest(String customerId, String accountNumber, String beneficiaryName,
                                              String beneficiaryAccountNumber, String beneficiaryBankCode,
                                              String beneficiaryBankName, String beneficiaryType) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(customerId);
        request.setAccountNumber(accountNumber);
        request.setBeneficiaryName(beneficiaryName);
        request.setBeneficiaryAccountNumber(beneficiaryAccountNumber);
        request.setBeneficiaryBankCode(beneficiaryBankCode);
        request.setBeneficiaryBankName(beneficiaryBankName);
        request.setBeneficiaryType(beneficiaryType);
        return request;
    }
    
    @Test
    @DisplayName("Should create beneficiary successfully")
    void shouldCreateBeneficiarySuccessfully() throws Exception {
        // Given
        BeneficiaryRequest request = createRequest("CUST001", "ACC001", "John Doe", "BEN001", 
                "BANK001", "Test Bank", "DOMESTIC");
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.beneficiaryName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        // Verify in database
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByCustomerId("CUST001");
        assertThat(beneficiaries).hasSize(1);
        assertThat(beneficiaries.get(0).getBeneficiaryName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should return 409 when creating duplicate beneficiary")
    void shouldReturn409WhenCreatingDuplicateBeneficiary() throws Exception {
        // Given
        BeneficiaryRequest request = createRequest("CUST002", "ACC002", "Jane Doe", "BEN002",
                "BANK002", "Test Bank 2", "INTERNATIONAL");
        
        // Create first beneficiary
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // When & Then - Try to create duplicate
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    @DisplayName("Should return 400 for invalid beneficiary type")
    void shouldReturn400ForInvalidBeneficiaryType() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "customerId": "CUST003",
                    "accountNumber": "ACC003",
                    "beneficiaryName": "Test User",
                    "beneficiaryAccountNumber": "BEN003",
                    "beneficiaryBankCode": "BANK003",
                    "beneficiaryBankName": "Test Bank",
                    "beneficiaryType": "INVALID"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should update beneficiary successfully")
    void shouldUpdateBeneficiarySuccessfully() throws Exception {
        // Given - Create a beneficiary first
        BeneficiaryRequest createRequest = createRequest("CUST004", "ACC004", "Original Name", "BEN004",
                "BANK004", "Original Bank", "DOMESTIC");
        
        String response = mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        BeneficiaryResponse created = objectMapper.readValue(response, BeneficiaryResponse.class);
        
        // When - Update the beneficiary
        BeneficiaryRequest updateRequest = createRequest("CUST004", "ACC004", "Updated Name", "BEN004",
                "BANK004", "Updated Bank", "DOMESTIC");
        
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", created.getId())
                        .param("customerId", "CUST004")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beneficiaryName").value("Updated Name"))
                .andExpect(jsonPath("$.beneficiaryBankName").value("Updated Bank"));
    }
    
    @Test
    @DisplayName("Should delete beneficiary successfully")
    void shouldDeleteBeneficiarySuccessfully() throws Exception {
        // Given - Create a beneficiary first
        BeneficiaryRequest createRequest = createRequest("CUST005", "ACC005", "To Delete", "BEN005",
                "BANK005", "Test Bank", "DOMESTIC");
        
        String response = mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        BeneficiaryResponse created = objectMapper.readValue(response, BeneficiaryResponse.class);
        
        // When - Delete the beneficiary
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", created.getId())
                        .param("customerId", "CUST005"))
                .andExpect(status().isNoContent());
        
        // Then - Verify it's marked as deleted
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", created.getId())
                        .param("customerId", "CUST005"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should get beneficiary by ID")
    void shouldGetBeneficiaryById() throws Exception {
        // Given
        BeneficiaryRequest createRequest = createRequest("CUST006", "ACC006", "Get By ID Test", "BEN006",
                "BANK006", "Test Bank", "DOMESTIC");
        
        String response = mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        BeneficiaryResponse created = objectMapper.readValue(response, BeneficiaryResponse.class);
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", created.getId())
                        .param("customerId", "CUST006"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.beneficiaryName").value("Get By ID Test"));
    }
    
    @Test
    @DisplayName("Should get all beneficiaries for customer")
    void shouldGetAllBeneficiariesForCustomer() throws Exception {
        // Given - Create multiple beneficiaries
        BeneficiaryRequest request1 = createRequest("CUST007", "ACC007", "Beneficiary 1", "BEN007A",
                "BANK007", "Test Bank", "DOMESTIC");
        
        BeneficiaryRequest request2 = createRequest("CUST007", "ACC007", "Beneficiary 2", "BEN007B",
                "BANK007", "Test Bank", "INTERNATIONAL");
        
        mockMvc.perform(post("/api/v1/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/v1/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    @DisplayName("Should get beneficiaries by customer and account number")
    void shouldGetBeneficiariesByCustomerAndAccountNumber() throws Exception {
        // Given
        BeneficiaryRequest request = createRequest("CUST008", "ACC008", "Account Filter Test", "BEN008",
                "BANK008", "Test Bank", "DOMESTIC");
        
        mockMvc.perform(post("/api/v1/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST008")
                        .param("accountNumber", "ACC008"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC008"));
    }
    
    @Test
    @DisplayName("Should enforce unique constraint on customer, beneficiary account, and status")
    void shouldEnforceUniqueConstraint() throws Exception {
        // Given
        BeneficiaryRequest request = createRequest("CUST009", "ACC009", "Constraint Test", "BEN009",
                "BANK009", "Test Bank", "DOMESTIC");
        
        // Create beneficiary
        String response = mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        BeneficiaryResponse created = objectMapper.readValue(response, BeneficiaryResponse.class);
        
        // Delete it (soft delete)
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", created.getId())
                        .param("customerId", "CUST009"))
                .andExpect(status().isNoContent());
        
        // Should be able to create same beneficiary again after deletion
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
    
    // ===== Negative Test Scenarios Using Pre-populated Data =====
    
    @Test
    @DisplayName("Should return 404 when getting non-existent beneficiary")
    void shouldReturn404WhenGettingNonExistentBeneficiary() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 99999)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 404 when updating non-existent beneficiary")
    void shouldReturn404WhenUpdatingNonExistentBeneficiary() throws Exception {
        // Given
        BeneficiaryRequest updateRequest = createRequest("CUST001", "ACC001", "Update Test", "BEN999",
                "BANK001", "Test Bank", "DOMESTIC");
        
        // When & Then
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", 99999)
                        .param("customerId", "CUST001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 404 when deleting non-existent beneficiary")
    void shouldReturn404WhenDeletingNonExistentBeneficiary() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", 99999)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should return 403 when accessing beneficiary of different customer")
    void shouldReturn403WhenAccessingBeneficiaryOfDifferentCustomer() throws Exception {
        // Given - ID 1 belongs to CUST001 (from init.db)
        
        // When & Then - Try to access with wrong customer ID
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 1)
                        .param("customerId", "WRONG_CUSTOMER"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 403 when updating beneficiary of different customer")
    void shouldReturn403WhenUpdatingBeneficiaryOfDifferentCustomer() throws Exception {
        // Given - ID 1 belongs to CUST001 (from init.db)
        BeneficiaryRequest updateRequest = createRequest("WRONG_CUSTOMER", "ACC999", "Hacker", "BEN999",
                "BANK999", "Test Bank", "DOMESTIC");
        
        // When & Then
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", 1)
                        .param("customerId", "WRONG_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 403 when deleting beneficiary of different customer")
    void shouldReturn403WhenDeletingBeneficiaryOfDifferentCustomer() throws Exception {
        // Given - ID 2 belongs to CUST006 (from init.db)
        
        // When & Then
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", 2)
                        .param("customerId", "WRONG_CUSTOMER"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Should return 409 when creating duplicate with pre-existing data")
    void shouldReturn409WhenCreatingDuplicateWithPreExistingData() throws Exception {
        // Given - CUST003 already has BEN003 (from init.db - ID 5)
        BeneficiaryRequest request = createRequest("CUST003", "ACC003", "Duplicate Test", "BEN003",
                "BANK004", "Test Bank", "DOMESTIC");
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @DisplayName("Should return 400 for missing required fields")
    void shouldReturn400ForMissingRequiredFields() throws Exception {
        // Given - Missing beneficiaryName
        String invalidRequest = """
                {
                    "customerId": "CUST010",
                    "accountNumber": "ACC010",
                    "beneficiaryAccountNumber": "BEN010",
                    "beneficiaryBankCode": "BANK010",
                    "beneficiaryBankName": "Test Bank",
                    "beneficiaryType": "DOMESTIC"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 for invalid bank code format")
    void shouldReturn400ForInvalidBankCodeFormat() throws Exception {
        // Given
        BeneficiaryRequest request = createRequest("CUST010", "ACC010", "Invalid Bank Code", "BEN010",
                "", "Test Bank", "DOMESTIC");  // Empty bank code
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return empty list for customer with no beneficiaries")
    void shouldReturnEmptyListForCustomerWithNoBeneficiaries() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST_NO_DATA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @DisplayName("Should retrieve existing beneficiaries from init.db")
    void shouldRetrieveExistingBeneficiariesFromInitDb() throws Exception {
        // Given - CUST002 has 3 beneficiaries in init.db (IDs 10, 11, 12)
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.beneficiaryAccountNumber=='BEN010')]").exists())
                .andExpect(jsonPath("$[?(@.beneficiaryAccountNumber=='BEN011')]").exists())
                .andExpect(jsonPath("$[?(@.beneficiaryAccountNumber=='BEN012')]").exists());
    }
    
    @Test
    @DisplayName("Should filter beneficiaries by account number using init.db data")
    void shouldFilterBeneficiariesByAccountNumberUsingInitDbData() throws Exception {
        // Given - CUST003 has beneficiaries with ACC_A and ACC_B in init.db
        
        // When & Then - Filter by ACC_A
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST003")
                        .param("accountNumber", "ACC_A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC_A"))
                .andExpect(jsonPath("$[1].accountNumber").value("ACC_A"));
    }
    
    @Test
    @DisplayName("Should update existing beneficiary from init.db")
    void shouldUpdateExistingBeneficiaryFromInitDb() throws Exception {
        // Given - ID 1 exists with "Old Name" (from init.db)
        BeneficiaryRequest updateRequest = createRequest("CUST001", "ACC001", "Updated Name", "BEN001",
                "BANK001", "Updated Bank", "DOMESTIC");
        
        // When
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", 1)
                        .param("customerId", "CUST001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beneficiaryName").value("Updated Name"))
                .andExpect(jsonPath("$.beneficiaryBankName").value("Updated Bank"));
        
        // Then - Verify the update persisted
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 1)
                        .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.beneficiaryName").value("Updated Name"));
    }
    
    @Test
    @DisplayName("Should delete existing beneficiary from init.db")
    void shouldDeleteExistingBeneficiaryFromInitDb() throws Exception {
        // Given - ID 4 exists and belongs to CUST003 (from init.db)
        
        // When
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", 4)
                        .param("customerId", "CUST003"))
                .andExpect(status().isNoContent());
        
        // Then - Verify it's soft deleted
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 4)
                        .param("customerId", "CUST003"))
                .andExpect(status().isNotFound());
    }
}
