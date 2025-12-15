package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.model.BeneficiaryAudit;
import com.alok.payment.beneficiaries.repository.BeneficiaryAuditRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryAuditService Unit Tests")
class BeneficiaryAuditServiceTest {
    
    @Mock
    private BeneficiaryAuditRepository auditRepository;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    private BeneficiaryAuditService auditService;
    
    private Beneficiary beneficiary;
    private BeneficiaryAudit audit;
    
    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime
        auditService = new BeneficiaryAuditService(auditRepository, objectMapper);
        
        beneficiary = new Beneficiary();
        beneficiary.setId(1L);
        beneficiary.setCustomerId("CUST001");
        beneficiary.setAccountNumber("ACC001");
        beneficiary.setBeneficiaryName("John Doe");
        beneficiary.setBeneficiaryAccountNumber("BEN001");
        beneficiary.setBeneficiaryBankCode("BANK001");
        beneficiary.setBeneficiaryBankName("Test Bank");
        beneficiary.setBeneficiaryType("DOMESTIC");
        beneficiary.setStatus("ACTIVE");
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        
        audit = new BeneficiaryAudit();
        audit.setId(100L);
        audit.setBeneficiaryId(1L);
        audit.setCustomerId("CUST001");
        audit.setOperation("CREATE");
        audit.setChanges("{}");
        audit.setPerformedBy("USER001");
        audit.setPerformedAt(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("Should audit beneficiary creation successfully")
    void shouldAuditBeneficiaryCreationSuccessfully() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        BeneficiaryAudit result = auditService.auditCreate(beneficiary, "USER001");
        
        // Then - Comprehensive field assertions
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(100L);
        assertThat(result.getBeneficiaryId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotBlank().isEqualTo("CUST001");
        assertThat(result.getOperation()).isNotBlank().isEqualTo("CREATE");
        assertThat(result.getPerformedBy()).isNotBlank().isEqualTo("USER001");
        assertThat(result.getPerformedAt()).isNotNull();
        
        // ArgumentCaptor verification
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getBeneficiaryId()).isEqualTo(1L);
        assertThat(savedAudit.getCustomerId()).isEqualTo("CUST001");
        assertThat(savedAudit.getOperation()).isEqualTo("CREATE");
        assertThat(savedAudit.getPerformedBy()).isEqualTo("USER001");
        assertThat(savedAudit.getPerformedAt()).isNotNull();
        assertThat(savedAudit.getChanges()).isNotNull().isNotBlank();
    }
    
    @Test
    @DisplayName("Should use SYSTEM as default performer when null is passed for create")
    void shouldUseSystemAsDefaultPerformerForCreate() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditCreate(beneficiary, null);
        
        // Then
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getPerformedBy()).isEqualTo("SYSTEM");
    }
    
    @Test
    @DisplayName("Should audit beneficiary update successfully")
    void shouldAuditBeneficiaryUpdateSuccessfully() {
        // Given
        audit.setOperation("UPDATE");
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        BeneficiaryAudit result = auditService.auditUpdate(beneficiary, "USER002");
        
        // Then - Comprehensive field assertions
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(100L);
        assertThat(result.getBeneficiaryId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotBlank().isEqualTo("CUST001");
        assertThat(result.getOperation()).isNotBlank().isEqualTo("UPDATE");
        assertThat(result.getPerformedBy()).isNotBlank();
        assertThat(result.getPerformedAt()).isNotNull();
        
        // ArgumentCaptor verification
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getBeneficiaryId()).isEqualTo(1L);
        assertThat(savedAudit.getCustomerId()).isEqualTo("CUST001");
        assertThat(savedAudit.getOperation()).isEqualTo("UPDATE");
        assertThat(savedAudit.getPerformedBy()).isEqualTo("USER002");
        assertThat(savedAudit.getPerformedAt()).isNotNull();
        assertThat(savedAudit.getChanges()).isNotNull().isNotBlank();
    }
    
    @Test
    @DisplayName("Should use SYSTEM as default performer when null is passed for update")
    void shouldUseSystemAsDefaultPerformerForUpdate() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditUpdate(beneficiary, null);
        
        // Then
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getPerformedBy()).isEqualTo("SYSTEM");
    }
    
    @Test
    @DisplayName("Should audit beneficiary deletion successfully")
    void shouldAuditBeneficiaryDeletionSuccessfully() {
        // Given
        audit.setOperation("DELETE");
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        BeneficiaryAudit result = auditService.auditDelete(1L, "CUST001", "USER003");
        
        // Then - Comprehensive field assertions
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(100L);
        assertThat(result.getBeneficiaryId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotBlank().isEqualTo("CUST001");
        assertThat(result.getOperation()).isNotBlank().isEqualTo("DELETE");
        assertThat(result.getPerformedBy()).isNotBlank();
        assertThat(result.getPerformedAt()).isNotNull();
        
        // ArgumentCaptor verification
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getBeneficiaryId()).isEqualTo(1L);
        assertThat(savedAudit.getCustomerId()).isEqualTo("CUST001");
        assertThat(savedAudit.getOperation()).isEqualTo("DELETE");
        assertThat(savedAudit.getPerformedBy()).isEqualTo("USER003");
        assertThat(savedAudit.getPerformedAt()).isNotNull();
        assertThat(savedAudit.getChanges()).isNotNull().isNotBlank();
    }
    
    @Test
    @DisplayName("Should use SYSTEM as default performer when null is passed for delete")
    void shouldUseSystemAsDefaultPerformerForDelete() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditDelete(1L, "CUST001", null);
        
        // Then
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getPerformedBy()).isEqualTo("SYSTEM");
    }
    
    @Test
    @DisplayName("Should get audit history for beneficiary and customer")
    void shouldGetAuditHistoryForBeneficiaryAndCustomer() {
        // Given
        List<BeneficiaryAudit> audits = Arrays.asList(audit, audit);
        when(auditRepository.findByBeneficiaryIdAndCustomerId(1L, "CUST001"))
                .thenReturn(audits);
        
        // When
        List<BeneficiaryAudit> result = auditService.getAuditHistory(1L, "CUST001");
        
        // Then - Verify non-null list
        assertThat(result).isNotNull().isNotEmpty().hasSize(2);
        verify(auditRepository).findByBeneficiaryIdAndCustomerId(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should get customer audit history")
    void shouldGetCustomerAuditHistory() {
        // Given
        List<BeneficiaryAudit> audits = Arrays.asList(audit, audit);
        when(auditRepository.findByCustomerIdOrderByPerformedAtDesc("CUST001"))
                .thenReturn(audits);
        
        // When
        List<BeneficiaryAudit> result = auditService.getCustomerAuditHistory("CUST001");
        
        // Then - Verify non-null list
        assertThat(result).isNotNull().isNotEmpty().hasSize(2);
        verify(auditRepository).findByCustomerIdOrderByPerformedAtDesc("CUST001");
    }
    
    @Test
    @DisplayName("Should return non-null empty list when no audit history found")
    void shouldReturnNonNullEmptyList() {
        // Given
        when(auditRepository.findByBeneficiaryIdAndCustomerId(1L, "CUST001"))
                .thenReturn(List.of());
        
        // When
        List<BeneficiaryAudit> result = auditService.getAuditHistory(1L, "CUST001");
        
        // Then
        assertThat(result).isNotNull().isEmpty();
    }
    
    @Test
    @DisplayName("Should serialize beneficiary to JSON in changes field")
    void shouldSerializeBeneficiaryToJson() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditCreate(beneficiary, "USER001");
        
        // Then
        ArgumentCaptor<BeneficiaryAudit> captor = ArgumentCaptor.forClass(BeneficiaryAudit.class);
        verify(auditRepository).save(captor.capture());
        
        BeneficiaryAudit savedAudit = captor.getValue();
        assertThat(savedAudit.getChanges()).contains("CUST001");
        assertThat(savedAudit.getChanges()).contains("John Doe");
        assertThat(savedAudit.getChanges()).contains("BEN001");
    }
    
    @Test
    @DisplayName("Should verify repository interactions for create audit")
    void shouldVerifyRepositoryInteractionsForCreate() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditCreate(beneficiary, "USER001");
        
        // Then
        verify(auditRepository, times(1)).save(any(BeneficiaryAudit.class));
        verifyNoMoreInteractions(auditRepository);
    }
    
    @Test
    @DisplayName("Should verify repository interactions for update audit")
    void shouldVerifyRepositoryInteractionsForUpdate() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditUpdate(beneficiary, "USER001");
        
        // Then
        verify(auditRepository, times(1)).save(any(BeneficiaryAudit.class));
        verifyNoMoreInteractions(auditRepository);
    }
    
    @Test
    @DisplayName("Should verify repository interactions for delete audit")
    void shouldVerifyRepositoryInteractionsForDelete() {
        // Given
        when(auditRepository.save(any(BeneficiaryAudit.class))).thenReturn(audit);
        
        // When
        auditService.auditDelete(1L, "CUST001", "USER001");
        
        // Then
        verify(auditRepository, times(1)).save(any(BeneficiaryAudit.class));
        verifyNoMoreInteractions(auditRepository);
    }
    
    @Test
    @DisplayName("Should verify repository interactions for get history")
    void shouldVerifyRepositoryInteractionsForGetHistory() {
        // Given
        when(auditRepository.findByBeneficiaryIdAndCustomerId(1L, "CUST001"))
                .thenReturn(List.of());
        
        // When
        auditService.getAuditHistory(1L, "CUST001");
        
        // Then
        verify(auditRepository, times(1)).findByBeneficiaryIdAndCustomerId(1L, "CUST001");
        verifyNoMoreInteractions(auditRepository);
    }
    
    @Test
    @DisplayName("Should verify repository interactions for get customer history")
    void shouldVerifyRepositoryInteractionsForGetCustomerHistory() {
        // Given
        when(auditRepository.findByCustomerIdOrderByPerformedAtDesc("CUST001"))
                .thenReturn(List.of());
        
        // When
        auditService.getCustomerAuditHistory("CUST001");
        
        // Then
        verify(auditRepository, times(1)).findByCustomerIdOrderByPerformedAtDesc("CUST001");
        verifyNoMoreInteractions(auditRepository);
    }
    
    @Test
    @DisplayName("Should return list with correct content for audit history")
    void shouldReturnListWithCorrectContent() {
        // Given
        BeneficiaryAudit audit1 = new BeneficiaryAudit();
        audit1.setId(1L);
        audit1.setOperation("CREATE");
        
        BeneficiaryAudit audit2 = new BeneficiaryAudit();
        audit2.setId(2L);
        audit2.setOperation("UPDATE");
        
        when(auditRepository.findByCustomerIdOrderByPerformedAtDesc("CUST001"))
                .thenReturn(Arrays.asList(audit1, audit2));
        
        // When
        List<BeneficiaryAudit> result = auditService.getCustomerAuditHistory("CUST001");
        
        // Then - Verify list content, not just size
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(BeneficiaryAudit::getId)
                .containsExactly(1L, 2L);
        
        assertThat(result)
                .extracting(BeneficiaryAudit::getOperation)
                .containsExactly("CREATE", "UPDATE");
    }
}
