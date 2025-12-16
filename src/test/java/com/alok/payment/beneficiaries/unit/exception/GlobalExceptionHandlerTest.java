package com.alok.payment.beneficiaries.unit.exception;

import com.alok.payment.beneficiaries.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;
    
    @Mock
    private WebRequest webRequest;
    
    @Test
    @DisplayName("Should handle BeneficiaryNotFoundException")
    void shouldHandleBeneficiaryNotFoundException() {
        // Given
        BeneficiaryNotFoundException exception = new BeneficiaryNotFoundException("Beneficiary not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/beneficiaries/1");
        
        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBeneficiaryNotFoundException(exception, webRequest);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Beneficiary not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/beneficiaries/1");
    }
    
    @Test
    @DisplayName("Should handle DuplicateBeneficiaryException")
    void shouldHandleDuplicateBeneficiaryException() {
        // Given
        DuplicateBeneficiaryException exception = new DuplicateBeneficiaryException("Beneficiary already exists");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/beneficiaries");
        
        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDuplicateBeneficiaryException(exception, webRequest);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("Beneficiary already exists");
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
    }
    
    @Test
    @DisplayName("Should handle BeneficiaryValidationException")
    void shouldHandleBeneficiaryValidationException() {
        // Given
        BeneficiaryValidationException exception = new BeneficiaryValidationException("Validation failed");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/beneficiaries");
        
        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBeneficiaryValidationException(exception, webRequest);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
    }
    
    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/beneficiaries");
        
        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGlobalException(exception, webRequest);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }
}
