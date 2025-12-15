package com.alok.payment.beneficiaries.slice;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryValidationException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import com.alok.payment.beneficiaries.service.BeneficiaryValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Spring Data JDBC slice tests for BeneficiaryService.
 * Tests the service layer with real database interactions using Testcontainers.
 */
@DataJdbcTest
@Testcontainers
@Import(BeneficiaryService.class)
@DisplayName("BeneficiaryService Spring Slice Tests")
class BeneficiaryServiceSliceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("ghcr.io/alokkulkarni/testcontainers-registry/testcontainers/postgres:16-alpine")
                    .asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("beneficiaries_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.db");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    
    @MockBean
    private BeneficiaryValidationService validationService;
    
    @Autowired
    private BeneficiaryService beneficiaryService;
    
    private BeneficiaryRequest request;
    
    @BeforeEach
    void setUp() {
        request = new BeneficiaryRequest();
        request.setCustomerId("SLICE_CUST001");
        request.setAccountNumber("SLICE_ACC001");
        request.setBeneficiaryName("Slice Test User");
        request.setBeneficiaryAccountNumber("12345678901");
        request.setBeneficiaryBankCode("SLICEBNK");
        request.setBeneficiaryBankName("Slice Test Bank");
        request.setBeneficiaryType("DOMESTIC");
        
        // Default behavior: validation passes
        doNothing().when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
    }
    
    @Test
    @DisplayName("Should save beneficiary to database after validation")
    void shouldSaveBeneficiaryToDatabase() {
        // When
        Beneficiary saved = beneficiaryService.createBeneficiary(request);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        
        // Verify it's actually in the database
        Optional<Beneficiary> found = beneficiaryRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getBeneficiaryName()).isEqualTo("Slice Test User");
        assertThat(found.get().getCustomerId()).isEqualTo("SLICE_CUST001");
    }
    
    @Test
    @DisplayName("Should not save to database when validation fails")
    void shouldNotSaveWhenValidationFails() {
        // Given
        doThrow(new BeneficiaryValidationException("Validation failed"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(request))
                .isInstanceOf(BeneficiaryValidationException.class);
        
        // Verify nothing was saved to database
        List<Beneficiary> all = beneficiaryRepository.findByCustomerId("SLICE_CUST001");
        assertThat(all).isEmpty();
    }
    
    @Test
    @DisplayName("Should retrieve beneficiaries for customer from database")
    void shouldRetrieveBeneficiariesFromDatabase() {
        // Given - create two beneficiaries
        beneficiaryService.createBeneficiary(request);
        
        BeneficiaryRequest request2 = new BeneficiaryRequest();
        request2.setCustomerId("SLICE_CUST001");
        request2.setAccountNumber("SLICE_ACC002");
        request2.setBeneficiaryName("Another User");
        request2.setBeneficiaryAccountNumber("98765432109");
        request2.setBeneficiaryBankCode("SLICEBNK");
        request2.setBeneficiaryBankName("Slice Test Bank");
        request2.setBeneficiaryType("INTERNATIONAL");
        
        beneficiaryService.createBeneficiary(request2);
        
        // When
        List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiaries("SLICE_CUST001", null);
        
        // Then
        assertThat(beneficiaries).hasSize(2);
        assertThat(beneficiaries)
                .extracting(Beneficiary::getBeneficiaryName)
                .containsExactlyInAnyOrder("Slice Test User", "Another User");
    }
    
    @Test
    @DisplayName("Should filter beneficiaries by account number")
    void shouldFilterBeneficiariesByAccountNumber() {
        // Given - create beneficiaries with different account numbers
        beneficiaryService.createBeneficiary(request);
        
        BeneficiaryRequest request2 = new BeneficiaryRequest();
        request2.setCustomerId("SLICE_CUST001");
        request2.setAccountNumber("SLICE_ACC002");
        request2.setBeneficiaryName("Different Account");
        request2.setBeneficiaryAccountNumber("11111111111");
        request2.setBeneficiaryBankCode("SLICEBNK");
        request2.setBeneficiaryBankName("Slice Test Bank");
        request2.setBeneficiaryType("DOMESTIC");
        
        beneficiaryService.createBeneficiary(request2);
        
        // When - filter by account number
        List<Beneficiary> filtered = beneficiaryService.getBeneficiaries("SLICE_CUST001", "SLICE_ACC001");
        
        // Then
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getAccountNumber()).isEqualTo("SLICE_ACC001");
    }
    
    @Test
    @DisplayName("Should update beneficiary in database")
    void shouldUpdateBeneficiaryInDatabase() {
        // Given - create a beneficiary first
        Beneficiary created = beneficiaryService.createBeneficiary(request);
        Long id = created.getId();
        
        // When - update it
        BeneficiaryRequest updateRequest = new BeneficiaryRequest();
        updateRequest.setCustomerId("SLICE_CUST001");
        updateRequest.setAccountNumber("SLICE_ACC001");
        updateRequest.setBeneficiaryName("Updated Name");
        updateRequest.setBeneficiaryAccountNumber("12345678901");
        updateRequest.setBeneficiaryBankCode("SLICEBNK");
        updateRequest.setBeneficiaryBankName("Updated Bank");
        updateRequest.setBeneficiaryType("INTERNATIONAL");
        
        beneficiaryService.updateBeneficiary(id, "SLICE_CUST001", updateRequest);
        
        // Then - verify update persisted
        Beneficiary updated = beneficiaryService.getBeneficiary(id, "SLICE_CUST001");
        assertThat(updated.getBeneficiaryName()).isEqualTo("Updated Name");
        assertThat(updated.getBeneficiaryBankName()).isEqualTo("Updated Bank");
        assertThat(updated.getBeneficiaryType()).isEqualTo("INTERNATIONAL");
    }
    
    @Test
    @DisplayName("Should soft delete beneficiary from database")
    void shouldSoftDeleteBeneficiary() {
        // Given
        Beneficiary created = beneficiaryService.createBeneficiary(request);
        Long id = created.getId();
        
        // When
        beneficiaryService.deleteBeneficiary(id, "SLICE_CUST001");
        
        // Then - verify it's no longer retrievable
        assertThatThrownBy(() -> beneficiaryService.getBeneficiary(id, "SLICE_CUST001"))
                .hasMessageContaining("not found");
    }
    
    @Test
    @DisplayName("Should validate before saving duplicate check")
    void shouldValidateBeforeDuplicateCheck() {
        // Given - create first beneficiary
        beneficiaryService.createBeneficiary(request);
        
        // Configure validation to fail on second attempt
        doThrow(new BeneficiaryValidationException("Validation failed"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then - try to create duplicate
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(request))
                .isInstanceOf(BeneficiaryValidationException.class);
        
        // Verify only one exists in database
        List<Beneficiary> all = beneficiaryRepository.findByCustomerId("SLICE_CUST001");
        assertThat(all).hasSize(1);
    }
    
    @Test
    @DisplayName("Should handle transactions correctly when validation fails")
    void shouldHandleTransactionsWhenValidationFails() {
        // Given
        doThrow(new BeneficiaryValidationException("Fraud detected"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(request))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("Fraud detected");
        
        // Verify nothing committed to database
        List<Beneficiary> all = beneficiaryRepository.findByCustomerId("SLICE_CUST001");
        assertThat(all).isEmpty();
    }
    
    @Test
    @DisplayName("Should set created and updated timestamps")
    void shouldSetTimestamps() {
        // When
        Beneficiary saved = beneficiaryService.createBeneficiary(request);
        
        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(saved.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Should default beneficiary type to DOMESTIC when null")
    void shouldDefaultBeneficiaryTypeWhenNull() {
        // Given
        request.setBeneficiaryType(null);
        
        // When
        Beneficiary saved = beneficiaryService.createBeneficiary(request);
        
        // Then
        assertThat(saved.getBeneficiaryType()).isEqualTo("DOMESTIC");
        
        // Verify in database
        Beneficiary found = beneficiaryService.getBeneficiary(saved.getId(), "SLICE_CUST001");
        assertThat(found.getBeneficiaryType()).isEqualTo("DOMESTIC");
    }
}
