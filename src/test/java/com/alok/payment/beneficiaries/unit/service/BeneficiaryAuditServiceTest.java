package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.service.BeneficiaryAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryAuditService Tests")
class BeneficiaryAuditServiceTest {
    
    private BeneficiaryAuditService auditService;
    
    @BeforeEach
    void setUp() {
        auditService = new BeneficiaryAuditService();
    }
    
    @Test
    @DisplayName("Should log beneficiary creation")
    void shouldLogBeneficiaryCreation() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryCreated("CUST001", 1L, "ACC001")
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should log beneficiary update")
    void shouldLogBeneficiaryUpdate() {
        // Given
        Map<String, String> changes = new HashMap<>();
        changes.put("beneficiaryName", "Updated Name");
        changes.put("beneficiaryBankName", "Updated Bank");
        
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryUpdated("CUST001", 1L, changes)
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should log beneficiary deletion")
    void shouldLogBeneficiaryDeletion() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryDeleted("CUST001", 1L)
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle null changes map in update log")
    void shouldHandleNullChangesInUpdateLog() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryUpdated("CUST001", 1L, null)
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle empty changes map in update log")
    void shouldHandleEmptyChangesInUpdateLog() {
        // Given
        Map<String, String> emptyChanges = new HashMap<>();
        
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryUpdated("CUST001", 1L, emptyChanges)
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle null customer ID in creation log")
    void shouldHandleNullCustomerIdInCreationLog() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryCreated(null, 1L, "ACC001")
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle null beneficiary ID in creation log")
    void shouldHandleNullBeneficiaryIdInCreationLog() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryCreated("CUST001", null, "ACC001")
        ).doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Should handle null account number in creation log")
    void shouldHandleNullAccountNumberInCreationLog() {
        // When & Then - should not throw exception
        assertThatCode(() -> 
            auditService.logBeneficiaryCreated("CUST001", 1L, null)
        ).doesNotThrowAnyException();
    }
}
