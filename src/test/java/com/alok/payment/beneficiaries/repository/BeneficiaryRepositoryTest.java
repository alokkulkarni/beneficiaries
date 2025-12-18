package com.alok.payment.beneficiaries.repository;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BeneficiaryRepository using TestContainers.
 * Tests the new findAllByCustomerId method alongside existing repository methods.
 */
@Testcontainers
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BeneficiaryRepository Integration Tests")
class BeneficiaryRepositoryTest {

        private static final DockerImageName POSTGRES_IMAGE = DockerImageName
            .parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine")
            .asCompatibleSubstituteFor("postgres");

        @Container
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        beneficiaryRepository.deleteAll();
    }

    @Test
    @DisplayName("findAllByCustomerId should return all beneficiaries regardless of status")
    void testFindAllByCustomerId() {
        // Given: Create beneficiaries with different statuses
        String customerId = "CUST_TEST_001";
        
        Beneficiary active1 = createBeneficiary(customerId, "ACC001", "Active User 1", "111111", "ACTIVE");
        Beneficiary active2 = createBeneficiary(customerId, "ACC002", "Active User 2", "222222", "ACTIVE");
        Beneficiary inactive = createBeneficiary(customerId, "ACC003", "Inactive User", "333333", "INACTIVE");
        Beneficiary deleted = createBeneficiary(customerId, "ACC004", "Deleted User", "444444", "DELETED");
        
        beneficiaryRepository.save(active1);
        beneficiaryRepository.save(active2);
        beneficiaryRepository.save(inactive);
        beneficiaryRepository.save(deleted);
        
        // When: Find all beneficiaries for the customer
        List<Beneficiary> allBeneficiaries = beneficiaryRepository.findAllByCustomerId(customerId);
        
        // Then: Should return all 4 beneficiaries regardless of status
        assertThat(allBeneficiaries).hasSize(4);
        assertThat(allBeneficiaries)
                .extracting(Beneficiary::getStatus)
                .containsExactlyInAnyOrder("ACTIVE", "ACTIVE", "INACTIVE", "DELETED");
    }

    @Test
    @DisplayName("findByCustomerId should return only ACTIVE beneficiaries")
    void testFindByCustomerIdReturnsOnlyActive() {
        // Given: Create beneficiaries with different statuses
        String customerId = "CUST_TEST_002";
        
        Beneficiary active1 = createBeneficiary(customerId, "ACC001", "Active User 1", "111111", "ACTIVE");
        Beneficiary active2 = createBeneficiary(customerId, "ACC002", "Active User 2", "222222", "ACTIVE");
        Beneficiary inactive = createBeneficiary(customerId, "ACC003", "Inactive User", "333333", "INACTIVE");
        
        beneficiaryRepository.save(active1);
        beneficiaryRepository.save(active2);
        beneficiaryRepository.save(inactive);
        
        // When: Find beneficiaries for the customer (only active)
        List<Beneficiary> activeBeneficiaries = beneficiaryRepository.findByCustomerId(customerId);
        
        // Then: Should return only 2 ACTIVE beneficiaries
        assertThat(activeBeneficiaries).hasSize(2);
        assertThat(activeBeneficiaries)
                .allMatch(b -> "ACTIVE".equals(b.getStatus()));
    }

    @Test
    @DisplayName("findAllByCustomerId should filter by customerId correctly")
    void testFindAllByCustomerIdFiltersCorrectly() {
        // Given: Create beneficiaries for different customers
        String customerId1 = "CUST_A";
        String customerId2 = "CUST_B";
        
        beneficiaryRepository.save(createBeneficiary(customerId1, "ACC001", "User A1", "111111", "ACTIVE"));
        beneficiaryRepository.save(createBeneficiary(customerId1, "ACC002", "User A2", "222222", "INACTIVE"));
        beneficiaryRepository.save(createBeneficiary(customerId2, "ACC003", "User B1", "333333", "ACTIVE"));
        
        // When: Find all beneficiaries for CUST_A
        List<Beneficiary> customer1Beneficiaries = beneficiaryRepository.findAllByCustomerId(customerId1);
        
        // Then: Should return only beneficiaries for CUST_A
        assertThat(customer1Beneficiaries).hasSize(2);
        assertThat(customer1Beneficiaries)
                .allMatch(b -> customerId1.equals(b.getCustomerId()));
    }

    @Test
    @DisplayName("findAllByCustomerId should return empty list for non-existent customer")
    void testFindAllByCustomerIdReturnsEmptyForNonExistentCustomer() {
        // Given: Some beneficiaries exist but not for the target customer
        beneficiaryRepository.save(createBeneficiary("CUST_OTHER", "ACC001", "Other User", "111111", "ACTIVE"));
        
        // When: Find all beneficiaries for non-existent customer
        List<Beneficiary> beneficiaries = beneficiaryRepository.findAllByCustomerId("NON_EXISTENT");
        
        // Then: Should return empty list
        assertThat(beneficiaries).isEmpty();
    }

    @Test
    @DisplayName("findAllByCustomerId should handle customer with only deleted beneficiaries")
    void testFindAllByCustomerIdWithOnlyDeletedBeneficiaries() {
        // Given: Create only deleted beneficiaries for a customer
        String customerId = "CUST_DELETED";
        
        beneficiaryRepository.save(createBeneficiary(customerId, "ACC001", "Deleted 1", "111111", "DELETED"));
        beneficiaryRepository.save(createBeneficiary(customerId, "ACC002", "Deleted 2", "222222", "DELETED"));
        
        // When: Find all beneficiaries
        List<Beneficiary> allBeneficiaries = beneficiaryRepository.findAllByCustomerId(customerId);
        
        // Then: Should return all deleted beneficiaries
        assertThat(allBeneficiaries).hasSize(2);
        assertThat(allBeneficiaries)
                .allMatch(b -> "DELETED".equals(b.getStatus()));
        
        // And: findByCustomerId should return empty (only active)
        List<Beneficiary> activeBeneficiaries = beneficiaryRepository.findByCustomerId(customerId);
        assertThat(activeBeneficiaries).isEmpty();
    }

    private Beneficiary createBeneficiary(String customerId, String accountNumber, 
                                         String name, String beneficiaryAccountNumber, 
                                         String status) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerId(customerId);
        beneficiary.setAccountNumber(accountNumber);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setBeneficiaryAccountNumber(beneficiaryAccountNumber);
        beneficiary.setBeneficiaryBankCode("BANK001");
        beneficiary.setBeneficiaryBankName("Test Bank");
        beneficiary.setBeneficiaryType("DOMESTIC");
        beneficiary.setStatus(status);
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        return beneficiary;
    }
}
