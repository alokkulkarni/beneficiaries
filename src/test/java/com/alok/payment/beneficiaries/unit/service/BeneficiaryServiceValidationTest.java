package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryValidationException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import com.alok.payment.beneficiaries.service.BeneficiaryValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BeneficiaryService focusing on validation service integration.
 * Tests validation failures, edge cases, and error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryService Validation Integration Tests")
class BeneficiaryServiceValidationTest {
    
    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    
    @Mock
    private BeneficiaryValidationService validationService;
    
    @InjectMocks
    private BeneficiaryService beneficiaryService;
    
    private BeneficiaryRequest validRequest;
    
    @BeforeEach
    void setUp() {
        validRequest = new BeneficiaryRequest();
        validRequest.setCustomerId("CUST001");
        validRequest.setAccountNumber("ACC001");
        validRequest.setBeneficiaryName("John Doe");
        validRequest.setBeneficiaryAccountNumber("12345678");
        validRequest.setBeneficiaryBankCode("BANK001");
        validRequest.setBeneficiaryBankName("Test Bank");
        validRequest.setBeneficiaryType("DOMESTIC");
    }
    
    @Test
    @DisplayName("Should call validation service before creating beneficiary")
    void shouldCallValidationServiceBeforeCreating() {
        // Given
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> {
                    Beneficiary b = invocation.getArgument(0);
                    b.setId(1L);
                    return b;
                });
        
        // When
        beneficiaryService.createBeneficiary(validRequest);
        
        // Then
        verify(validationService, times(1)).validateBeneficiary(validRequest);
        verify(beneficiaryRepository, times(1)).save(any(Beneficiary.class));
    }
    
    @Test
    @DisplayName("Should not create beneficiary when validation fails")
    void shouldNotCreateBeneficiaryWhenValidationFails() {
        // Given
        doThrow(new BeneficiaryValidationException("Invalid account number"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("Invalid account number");
        
        verify(validationService, times(1)).validateBeneficiary(validRequest);
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
        verify(beneficiaryRepository, never()).findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should validate before checking for duplicates")
    void shouldValidateBeforeCheckingDuplicates() {
        // Given - validation will fail
        doThrow(new BeneficiaryValidationException("Failed validation"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class);
        
        // Verify validation was called but duplicate check was not
        verify(validationService).validateBeneficiary(validRequest);
        verify(beneficiaryRepository, never()).findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should propagate validation exception with original message")
    void shouldPropagateValidationExceptionWithOriginalMessage() {
        // Given
        String errorMessage = "Beneficiary is on sanctions list";
        doThrow(new BeneficiaryValidationException(errorMessage))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessage(errorMessage);
    }
    
    @Test
    @DisplayName("Should handle validation service throwing runtime exception")
    void shouldHandleValidationServiceRuntimeException() {
        // Given
        doThrow(new RuntimeException("Unexpected error in validation service"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error");
        
        verify(validationService).validateBeneficiary(validRequest);
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }
    
    @Test
    @DisplayName("Should successfully create when validation passes and no duplicates")
    void shouldSuccessfullyCreateWhenValidationPassesAndNoDuplicates() {
        // Given
        doNothing().when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> {
                    Beneficiary b = invocation.getArgument(0);
                    b.setId(1L);
                    return b;
                });
        
        // When
        Beneficiary result = beneficiaryService.createBeneficiary(validRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(validationService).validateBeneficiary(validRequest);
        verify(beneficiaryRepository).findByCustomerIdAndBeneficiaryAccountNumber("CUST001", "12345678");
        verify(beneficiaryRepository).save(any(Beneficiary.class));
    }
    
    @Test
    @DisplayName("Should handle null beneficiary type during validation")
    void shouldHandleNullBeneficiaryTypeDuringValidation() {
        // Given
        validRequest.setBeneficiaryType(null);
        doNothing().when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Beneficiary result = beneficiaryService.createBeneficiary(validRequest);
        
        // Then
        assertThat(result.getBeneficiaryType()).isEqualTo("DOMESTIC");
        verify(validationService).validateBeneficiary(validRequest);
    }
    
    @Test
    @DisplayName("Should pass correct request to validation service")
    void shouldPassCorrectRequestToValidationService() {
        // Given
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        beneficiaryService.createBeneficiary(validRequest);
        
        // Then - verify exact request object is passed
        verify(validationService).validateBeneficiary(eq(validRequest));
    }
    
    @Test
    @DisplayName("Should validate international beneficiary type")
    void shouldValidateInternationalBeneficiaryType() {
        // Given
        validRequest.setBeneficiaryType("INTERNATIONAL");
        doNothing().when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> {
                    Beneficiary b = invocation.getArgument(0);
                    b.setId(1L);
                    return b;
                });
        
        // When
        Beneficiary result = beneficiaryService.createBeneficiary(validRequest);
        
        // Then
        assertThat(result.getBeneficiaryType()).isEqualTo("INTERNATIONAL");
        verify(validationService).validateBeneficiary(validRequest);
    }
    
    @Test
    @DisplayName("Should fail validation with empty beneficiary name")
    void shouldFailValidationWithEmptyBeneficiaryName() {
        // Given
        validRequest.setBeneficiaryName("");
        doThrow(new BeneficiaryValidationException("Beneficiary name is required"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("name is required");
    }
    
    @Test
    @DisplayName("Should fail validation with invalid account number format")
    void shouldFailValidationWithInvalidAccountNumberFormat() {
        // Given
        validRequest.setBeneficiaryAccountNumber("INVALID");
        doThrow(new BeneficiaryValidationException("Invalid account number format"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("Invalid account number format");
    }
    
    @Test
    @DisplayName("Should fail validation with invalid bank code")
    void shouldFailValidationWithInvalidBankCode() {
        // Given
        validRequest.setBeneficiaryBankCode("123");
        doThrow(new BeneficiaryValidationException("Invalid bank code format"))
                .when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        
        // When & Then
        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(validRequest))
                .isInstanceOf(BeneficiaryValidationException.class)
                .hasMessageContaining("Invalid bank code format");
    }
    
    @Test
    @DisplayName("Should verify validation service is called exactly once per create attempt")
    void shouldVerifyValidationServiceCalledExactlyOnce() {
        // Given
        doNothing().when(validationService).validateBeneficiary(any(BeneficiaryRequest.class));
        when(beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        beneficiaryService.createBeneficiary(validRequest);
        
        // Then - verify called exactly once, not more
        verify(validationService, times(1)).validateBeneficiary(validRequest);
        verifyNoMoreInteractions(validationService);
    }
}
