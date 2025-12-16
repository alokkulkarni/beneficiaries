package com.alok.payment.beneficiaries.unit.exception;

import com.alok.payment.beneficiaries.exception.BeneficiaryNotFoundException;
import com.alok.payment.beneficiaries.exception.BeneficiaryValidationException;
import com.alok.payment.beneficiaries.exception.DuplicateBeneficiaryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exception Classes Tests")
class ExceptionClassesTest {
    
    @Test
    @DisplayName("BeneficiaryNotFoundException should contain message")
    void beneficiaryNotFoundExceptionShouldContainMessage() {
        // Given
        String message = "Beneficiary not found with ID: 1";
        
        // When
        BeneficiaryNotFoundException exception = new BeneficiaryNotFoundException(message);
        
        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("DuplicateBeneficiaryException should contain message")
    void duplicateBeneficiaryExceptionShouldContainMessage() {
        // Given
        String message = "Beneficiary already exists with account number 123456";
        
        // When
        DuplicateBeneficiaryException exception = new DuplicateBeneficiaryException(message);
        
        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("BeneficiaryValidationException should contain message")
    void beneficiaryValidationExceptionShouldContainMessage() {
        // Given
        String message = "Validation failed for beneficiary";
        
        // When
        BeneficiaryValidationException exception = new BeneficiaryValidationException(message);
        
        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
    
    @Test
    @DisplayName("BeneficiaryValidationException with cause should contain message and cause")
    void beneficiaryValidationExceptionWithCauseShouldContainMessageAndCause() {
        // Given
        String message = "Validation failed for beneficiary";
        Throwable cause = new IllegalArgumentException("Invalid account number");
        
        // When
        BeneficiaryValidationException exception = new BeneficiaryValidationException(message, cause);
        
        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("BeneficiaryNotFoundException can be thrown and caught")
    void beneficiaryNotFoundExceptionCanBeThrownAndCaught() {
        // When & Then
        assertThatThrownBy(() -> {
            throw new BeneficiaryNotFoundException("Not found");
        })
        .isInstanceOf(BeneficiaryNotFoundException.class)
        .hasMessage("Not found");
    }
    
    @Test
    @DisplayName("DuplicateBeneficiaryException can be thrown and caught")
    void duplicateBeneficiaryExceptionCanBeThrownAndCaught() {
        // When & Then
        assertThatThrownBy(() -> {
            throw new DuplicateBeneficiaryException("Duplicate");
        })
        .isInstanceOf(DuplicateBeneficiaryException.class)
        .hasMessage("Duplicate");
    }
    
    @Test
    @DisplayName("BeneficiaryValidationException can be thrown and caught")
    void beneficiaryValidationExceptionCanBeThrownAndCaught() {
        // When & Then
        assertThatThrownBy(() -> {
            throw new BeneficiaryValidationException("Validation failed");
        })
        .isInstanceOf(BeneficiaryValidationException.class)
        .hasMessage("Validation failed");
    }
}
