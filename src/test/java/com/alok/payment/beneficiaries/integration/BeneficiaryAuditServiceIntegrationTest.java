package com.alok.payment.beneficiaries.integration;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.model.BeneficiaryAudit;
import com.alok.payment.beneficiaries.repository.BeneficiaryAuditRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("BeneficiaryAuditService Integration Tests")
class BeneficiaryAuditServiceIntegrationTest {
    
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
    private BeneficiaryAuditService auditService;
    
    @Autowired
    private BeneficiaryAuditRepository auditRepository;
    
    @BeforeEach
    void setUp() {
        // Clean up audit records before each test
        auditRepository.deleteAll();
    }
    
    private Beneficiary createTestBeneficiary(Long id, String customerId, String beneficiaryName) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(id);
        beneficiary.setCustomerId(customerId);
        beneficiary.setAccountNumber("ACC001");
        beneficiary.setBeneficiaryName(beneficiaryName);
        beneficiary.setBeneficiaryAccountNumber("BEN001");
        beneficiary.setBeneficiaryBankCode("BANK001");
        beneficiary.setBeneficiaryBankName("Test Bank");
        beneficiary.setBeneficiaryType("DOMESTIC");
        beneficiary.setStatus("ACTIVE");
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        return beneficiary;
    }
    
    @Test
    @DisplayName("Should persist create audit to database")
    void shouldPersistCreateAuditToDatabase() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(1L, "CUST001", "John Doe");
        
        // When
        BeneficiaryAudit audit = auditService.auditCreate(beneficiary, "USER001");
        
        // Then
        assertThat(audit.getId()).isNotNull();
        
        // Verify it was actually saved to database
        BeneficiaryAudit saved = auditRepository.findById(audit.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getBeneficiaryId()).isEqualTo(1L);
        assertThat(saved.getCustomerId()).isEqualTo("CUST001");
        assertThat(saved.getOperation()).isEqualTo("CREATE");
        assertThat(saved.getPerformedBy()).isEqualTo("USER001");
        assertThat(saved.getChanges()).contains("John Doe");
    }
    
    @Test
    @DisplayName("Should persist update audit to database")
    void shouldPersistUpdateAuditToDatabase() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(2L, "CUST002", "Jane Smith");
        
        // When
        BeneficiaryAudit audit = auditService.auditUpdate(beneficiary, "USER002");
        
        // Then
        assertThat(audit.getId()).isNotNull();
        
        // Verify it was actually saved to database
        BeneficiaryAudit saved = auditRepository.findById(audit.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getBeneficiaryId()).isEqualTo(2L);
        assertThat(saved.getCustomerId()).isEqualTo("CUST002");
        assertThat(saved.getOperation()).isEqualTo("UPDATE");
        assertThat(saved.getPerformedBy()).isEqualTo("USER002");
        assertThat(saved.getChanges()).contains("Jane Smith");
    }
    
    @Test
    @DisplayName("Should persist delete audit to database")
    void shouldPersistDeleteAuditToDatabase() {
        // When
        BeneficiaryAudit audit = auditService.auditDelete(3L, "CUST003", "USER003");
        
        // Then
        assertThat(audit.getId()).isNotNull();
        
        // Verify it was actually saved to database
        BeneficiaryAudit saved = auditRepository.findById(audit.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getBeneficiaryId()).isEqualTo(3L);
        assertThat(saved.getCustomerId()).isEqualTo("CUST003");
        assertThat(saved.getOperation()).isEqualTo("DELETE");
        assertThat(saved.getPerformedBy()).isEqualTo("USER003");
    }
    
    @Test
    @DisplayName("Should retrieve audit history for beneficiary and customer")
    void shouldRetrieveAuditHistoryForBeneficiaryAndCustomer() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(4L, "CUST004", "Bob Johnson");
        auditService.auditCreate(beneficiary, "USER001");
        auditService.auditUpdate(beneficiary, "USER002");
        
        // When
        List<BeneficiaryAudit> history = auditService.getAuditHistory(4L, "CUST004");
        
        // Then
        assertThat(history).hasSize(2);
        assertThat(history).extracting(BeneficiaryAudit::getOperation)
                .containsExactlyInAnyOrder("CREATE", "UPDATE");
    }
    
    @Test
    @DisplayName("Should retrieve customer audit history")
    void shouldRetrieveCustomerAuditHistory() {
        // Given
        Beneficiary beneficiary1 = createTestBeneficiary(5L, "CUST005", "Alice Brown");
        Beneficiary beneficiary2 = createTestBeneficiary(6L, "CUST005", "Charlie Green");
        
        auditService.auditCreate(beneficiary1, "USER001");
        auditService.auditCreate(beneficiary2, "USER001");
        auditService.auditUpdate(beneficiary1, "USER002");
        
        // When
        List<BeneficiaryAudit> history = auditService.getCustomerAuditHistory("CUST005");
        
        // Then
        assertThat(history).hasSize(3);
        assertThat(history).extracting(BeneficiaryAudit::getOperation)
                .containsExactlyInAnyOrder("CREATE", "CREATE", "UPDATE");
    }
    
    @Test
    @DisplayName("Should serialize complete beneficiary object in changes field")
    void shouldSerializeCompleteBeneficiaryObject() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(7L, "CUST006", "David White");
        
        // When
        BeneficiaryAudit audit = auditService.auditCreate(beneficiary, "USER001");
        
        // Then
        BeneficiaryAudit saved = auditRepository.findById(audit.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getChanges()).contains("CUST006");
        assertThat(saved.getChanges()).contains("David White");
        assertThat(saved.getChanges()).contains("BEN001");
        assertThat(saved.getChanges()).contains("BANK001");
        assertThat(saved.getChanges()).contains("DOMESTIC");
    }
    
    @Test
    @DisplayName("Should use SYSTEM as default performer when null")
    void shouldUseSystemAsDefaultPerformer() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(8L, "CUST007", "Eve Black");
        
        // When
        BeneficiaryAudit audit = auditService.auditCreate(beneficiary, null);
        
        // Then
        BeneficiaryAudit saved = auditRepository.findById(audit.getId()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getPerformedBy()).isEqualTo("SYSTEM");
    }
    
    @Test
    @DisplayName("Should handle multiple audit operations in sequence")
    void shouldHandleMultipleAuditOperationsInSequence() {
        // Given
        Beneficiary beneficiary = createTestBeneficiary(9L, "CUST008", "Frank Miller");
        
        // When
        auditService.auditCreate(beneficiary, "USER001");
        auditService.auditUpdate(beneficiary, "USER002");
        auditService.auditDelete(9L, "CUST008", "USER003");
        
        // Then
        List<BeneficiaryAudit> history = auditService.getAuditHistory(9L, "CUST008");
        assertThat(history).hasSize(3);
        
        // Verify operations are present (order may vary due to timestamps)
        assertThat(history).extracting(BeneficiaryAudit::getOperation)
                .containsExactlyInAnyOrder("CREATE", "UPDATE", "DELETE");
        
        // Verify performers
        assertThat(history).extracting(BeneficiaryAudit::getPerformedBy)
                .containsExactlyInAnyOrder("USER001", "USER002", "USER003");
    }
    
    @Test
    @DisplayName("Should return empty list when no audit history exists")
    void shouldReturnEmptyListWhenNoAuditHistoryExists() {
        // When
        List<BeneficiaryAudit> history = auditService.getAuditHistory(999L, "NONEXISTENT");
        
        // Then
        assertThat(history).isNotNull().isEmpty();
    }
    
    @Test
    @DisplayName("Should order customer audit history by performed_at descending")
    void shouldOrderCustomerAuditHistoryByPerformedAtDesc() {
        // Given
        Beneficiary beneficiary1 = createTestBeneficiary(10L, "CUST009", "Grace Lee");
        Beneficiary beneficiary2 = createTestBeneficiary(11L, "CUST009", "Henry Wilson");
        
        BeneficiaryAudit first = auditService.auditCreate(beneficiary1, "USER001");
        BeneficiaryAudit second = auditService.auditCreate(beneficiary2, "USER002");
        BeneficiaryAudit third = auditService.auditUpdate(beneficiary1, "USER003");
        
        // When
        List<BeneficiaryAudit> history = auditService.getCustomerAuditHistory("CUST009");
        
        // Then
        assertThat(history).hasSize(3);
        // Verify descending order by checking timestamps
        for (int i = 0; i < history.size() - 1; i++) {
            assertThat(history.get(i).getPerformedAt())
                .isAfterOrEqualTo(history.get(i + 1).getPerformedAt());
        }
    }
}
