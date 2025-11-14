package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryNotFoundException;
import com.alok.payment.beneficiaries.exception.DuplicateBeneficiaryException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryService Unit Tests")
class BeneficiaryServiceTest {
    
    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    
    @InjectMocks
    private BeneficiaryService beneficiaryService;
    
    private BeneficiaryRequest beneficiaryRequest;
    private Beneficiary beneficiary;
    
    @BeforeEach
    void setUp() {
        beneficiaryRequest = new BeneficiaryRequest();
        beneficiaryRequest.setCustomerId("CUST001");
        beneficiaryRequest.setAccountNumber("ACC001");
        beneficiaryRequest.setBeneficiaryName("John Doe");
        beneficiaryRequest.setBeneficiaryAccountNumber("BEN001");
        beneficiaryRequest.setBeneficiaryBankCode("BANK001");
        beneficiaryRequest.setBeneficiaryBankName("Test Bank");
        beneficiaryRequest.setBeneficiaryType("DOMESTIC");
        
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
    }
    
    @Test
    @DisplayName("Should create beneficiary successfully")
    void shouldCreateBeneficiarySuccessfully() {
        // Given
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(
                anyString(), anyString())).thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);
        
        // When
        Beneficiary result = beneficiaryService.createBeneficiary(beneficiaryRequest);
        
        // Then - Comprehensive field assertions for EmptyObjectReturnValsMutator
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotBlank().isEqualTo("CUST001");
        assertThat(result.getAccountNumber()).isNotBlank().isEqualTo("ACC001");
        assertThat(result.getBeneficiaryName()).isNotBlank().isEqualTo("John Doe");
        assertThat(result.getBeneficiaryAccountNumber()).isNotBlank().isEqualTo("BEN001");
        assertThat(result.getBeneficiaryBankCode()).isNotBlank().isEqualTo("BANK001");
        assertThat(result.getBeneficiaryBankName()).isNotBlank().isEqualTo("Test Bank");
        assertThat(result.getBeneficiaryType()).isNotBlank().isEqualTo("DOMESTIC");
        assertThat(result.getStatus()).isNotBlank().isEqualTo("ACTIVE");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        
        verify(beneficiaryRepository).findByCustomerIdAndBeneficiaryAccountNumber("CUST001", "BEN001");
        
        // ArgumentCaptor verification for VoidMethodCallMutator
        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        
        Beneficiary savedBeneficiary = captor.getValue();
        assertThat(savedBeneficiary.getCustomerId()).isEqualTo("CUST001");
        assertThat(savedBeneficiary.getAccountNumber()).isEqualTo("ACC001");
        assertThat(savedBeneficiary.getBeneficiaryName()).isEqualTo("John Doe");
        assertThat(savedBeneficiary.getBeneficiaryAccountNumber()).isEqualTo("BEN001");
        assertThat(savedBeneficiary.getBeneficiaryBankCode()).isEqualTo("BANK001");
        assertThat(savedBeneficiary.getBeneficiaryBankName()).isEqualTo("Test Bank");
        assertThat(savedBeneficiary.getBeneficiaryType()).isEqualTo("DOMESTIC");
        assertThat(savedBeneficiary.getStatus()).isEqualTo("ACTIVE");
        assertThat(savedBeneficiary.getCreatedAt()).isNotNull();
        assertThat(savedBeneficiary.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should throw exception when creating duplicate beneficiary")
    void shouldThrowExceptionWhenCreatingDuplicateBeneficiary() {
        // Given
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(
                anyString(), anyString())).thenReturn(Optional.of(beneficiary));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(beneficiaryRequest))
                .isInstanceOf(DuplicateBeneficiaryException.class)
                .hasMessageContaining("already exists");
        
        verify(beneficiaryRepository).findByCustomerIdAndBeneficiaryAccountNumber("CUST001", "BEN001");
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }
    
    @Test
    @DisplayName("Should update beneficiary successfully")
    void shouldUpdateBeneficiarySuccessfully() {
        // Given
        when(beneficiaryRepository.findByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);
        
        BeneficiaryRequest updateRequest = new BeneficiaryRequest();
        updateRequest.setCustomerId("CUST001");
        updateRequest.setAccountNumber("ACC001");
        updateRequest.setBeneficiaryName("Jane Doe");
        updateRequest.setBeneficiaryAccountNumber("BEN001");
        updateRequest.setBeneficiaryBankCode("BANK001");
        updateRequest.setBeneficiaryBankName("Test Bank");
        updateRequest.setBeneficiaryType("DOMESTIC");
        
        // When
        Beneficiary result = beneficiaryService.updateBeneficiary(1L, "CUST001", updateRequest);
        
        // Then - Comprehensive field assertions
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotBlank().isEqualTo("CUST001");
        assertThat(result.getBeneficiaryName()).isNotBlank();
        assertThat(result.getStatus()).isNotBlank().isEqualTo("ACTIVE");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        
        verify(beneficiaryRepository).findByIdAndCustomerId(1L, "CUST001");
        
        // ArgumentCaptor verification for VoidMethodCallMutator
        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        
        Beneficiary updatedBeneficiary = captor.getValue();
        assertThat(updatedBeneficiary.getId()).isEqualTo(1L);
        assertThat(updatedBeneficiary.getCustomerId()).isEqualTo("CUST001");
        assertThat(updatedBeneficiary.getAccountNumber()).isEqualTo("ACC001");
        assertThat(updatedBeneficiary.getBeneficiaryName()).isEqualTo("Jane Doe");
        assertThat(updatedBeneficiary.getBeneficiaryAccountNumber()).isEqualTo("BEN001");
        assertThat(updatedBeneficiary.getBeneficiaryBankCode()).isEqualTo("BANK001");
        assertThat(updatedBeneficiary.getBeneficiaryBankName()).isEqualTo("Test Bank");
        assertThat(updatedBeneficiary.getBeneficiaryType()).isEqualTo("DOMESTIC");
        assertThat(updatedBeneficiary.getStatus()).isEqualTo("ACTIVE");
        assertThat(updatedBeneficiary.getUpdatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent beneficiary")
    void shouldThrowExceptionWhenUpdatingNonExistentBeneficiary() {
        // Given
        when(beneficiaryRepository.findByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.updateBeneficiary(1L, "CUST001", beneficiaryRequest))
                .isInstanceOf(BeneficiaryNotFoundException.class)
                .hasMessageContaining("not found");
        
        verify(beneficiaryRepository).findByIdAndCustomerId(1L, "CUST001");
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }
    
    @Test
    @DisplayName("Should delete beneficiary successfully")
    void shouldDeleteBeneficiarySuccessfully() {
        // Given
        when(beneficiaryRepository.softDeleteByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(1);
        
        // When
        beneficiaryService.deleteBeneficiary(1L, "CUST001");
        
        // Then - Verify with exact parameters for VoidMethodCallMutator
        verify(beneficiaryRepository, times(1)).softDeleteByIdAndCustomerId(1L, "CUST001");
        verifyNoMoreInteractions(beneficiaryRepository);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent beneficiary")
    void shouldThrowExceptionWhenDeletingNonExistentBeneficiary() {
        // Given
        when(beneficiaryRepository.softDeleteByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(0);
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.deleteBeneficiary(1L, "CUST001"))
                .isInstanceOf(BeneficiaryNotFoundException.class)
                .hasMessageContaining("not found");
        
        // Verify delete was attempted exactly once
        verify(beneficiaryRepository, times(1)).softDeleteByIdAndCustomerId(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should get beneficiary by ID successfully")
    void shouldGetBeneficiaryByIdSuccessfully() {
        // Given
        when(beneficiaryRepository.findByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(Optional.of(beneficiary));
        
        // When
        Beneficiary result = beneficiaryService.getBeneficiary(1L, "CUST001");
        
        // Then - Explicit null checks for NullReturnValsMutator
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isEqualTo(1L);
        assertThat(result.getCustomerId()).isNotNull().isNotBlank().isEqualTo("CUST001");
        assertThat(result.getBeneficiaryName()).isNotNull().isNotBlank();
        assertThat(result.getStatus()).isNotNull().isNotBlank();
        
        verify(beneficiaryRepository).findByIdAndCustomerId(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should throw exception when getting non-existent beneficiary")
    void shouldThrowExceptionWhenGettingNonExistentBeneficiary() {
        // Given
        when(beneficiaryRepository.findByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.getBeneficiary(1L, "CUST001"))
                .isInstanceOf(BeneficiaryNotFoundException.class)
                .hasMessageContaining("not found");
        
        verify(beneficiaryRepository).findByIdAndCustomerId(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should get all beneficiaries for customer")
    void shouldGetAllBeneficiariesForCustomer() {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary, beneficiary);
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(beneficiaries);
        
        // When
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", null);
        
        // Then - Verify non-null list for NullReturnValsMutator
        assertThat(result).isNotNull().isNotEmpty().hasSize(2);
        verify(beneficiaryRepository).findByCustomerId("CUST001");
        verify(beneficiaryRepository, never()).findByCustomerIdAndAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should get beneficiaries by customer and account number")
    void shouldGetBeneficiariesByCustomerAndAccountNumber() {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary);
        when(beneficiaryRepository.findByCustomerIdAndAccountNumber("CUST001", "ACC001"))
                .thenReturn(beneficiaries);
        
        // When
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", "ACC001");
        
        // Then
        assertThat(result).isNotNull().isNotEmpty().hasSize(1);
        verify(beneficiaryRepository).findByCustomerIdAndAccountNumber("CUST001", "ACC001");
        verify(beneficiaryRepository, never()).findByCustomerId(anyString());
    }
    
    // Boundary condition tests for NegateConditionalsMutator
    
    @Test
    @DisplayName("Should handle null account number")
    void shouldHandleNullAccountNumber() {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary);
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(beneficiaries);
        
        // When - explicitly pass null
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", null);
        
        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(beneficiaryRepository).findByCustomerId("CUST001");
        verify(beneficiaryRepository, never()).findByCustomerIdAndAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should handle empty account number")
    void shouldHandleEmptyAccountNumber() {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary);
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(beneficiaries);
        
        // When - explicitly pass empty string
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", "");
        
        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(beneficiaryRepository).findByCustomerId("CUST001");
        verify(beneficiaryRepository, never()).findByCustomerIdAndAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should handle blank account number with spaces")
    void shouldHandleBlankAccountNumber() {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary);
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(beneficiaries);
        
        // When - explicitly pass blank string
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", "   ");
        
        // Then
        assertThat(result).isNotNull().hasSize(1);
        verify(beneficiaryRepository).findByCustomerId("CUST001");
        verify(beneficiaryRepository, never()).findByCustomerIdAndAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should use existing beneficiary type when request type is null")
    void shouldUseExistingTypeWhenRequestTypeIsNull() {
        // Given
        beneficiary.setBeneficiaryType("INTERNATIONAL");
        when(beneficiaryRepository.findByIdAndCustomerId(1L, "CUST001"))
                .thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        BeneficiaryRequest updateRequest = new BeneficiaryRequest();
        updateRequest.setCustomerId("CUST001");
        updateRequest.setAccountNumber("ACC001");
        updateRequest.setBeneficiaryName("Updated Name");
        updateRequest.setBeneficiaryAccountNumber("BEN001");
        updateRequest.setBeneficiaryBankCode("BANK001");
        updateRequest.setBeneficiaryBankName("Test Bank");
        beneficiaryRequest.setBeneficiaryType(null); // NULL TYPE
        
        // When
        beneficiaryService.updateBeneficiary(1L, "CUST001", updateRequest);
        
        // Then - should keep existing type
        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        assertThat(captor.getValue().getBeneficiaryType()).isEqualTo("INTERNATIONAL");
    }
    
    @Test
    @DisplayName("Should use DOMESTIC as default when creating with null type")
    void shouldUseDefaultTypeWhenCreatingWithNull() {
        // Given
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        beneficiaryRequest.setBeneficiaryType(null); // NULL TYPE
        
        // When
        beneficiaryService.createBeneficiary(beneficiaryRequest);
        
        // Then - should default to DOMESTIC
        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        assertThat(captor.getValue().getBeneficiaryType()).isEqualTo("DOMESTIC");
    }
    
    @Test
    @DisplayName("Should return list with correct content and size")
    void shouldReturnListWithCorrectContent() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setBeneficiaryName("John");
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setBeneficiaryName("Jane");
        
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(Arrays.asList(ben1, ben2));
        
        // When
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", null);
        
        // Then - Verify list content, not just size
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(Beneficiary::getId)
                .containsExactly(1L, 2L);
        
        assertThat(result)
                .extracting(Beneficiary::getBeneficiaryName)
                .containsExactly("John", "Jane");
    }
    
    @Test
    @DisplayName("Should return non-null empty list when no beneficiaries found")
    void shouldReturnNonNullEmptyList() {
        // Given
        when(beneficiaryRepository.findByCustomerId("CUST001"))
                .thenReturn(List.of()); // Empty but not null
        
        // When
        List<Beneficiary> result = beneficiaryService.getBeneficiaries("CUST001", null);
        
        // Then
        assertThat(result).isNotNull().isEmpty();
    }
}
