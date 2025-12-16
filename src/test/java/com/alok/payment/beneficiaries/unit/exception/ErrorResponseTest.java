package com.alok.payment.beneficiaries.unit.exception;

import com.alok.payment.beneficiaries.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {
    
    @Test
    @DisplayName("Should create ErrorResponse with all fields")
    void shouldCreateErrorResponseWithAllFields() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/v1/beneficiaries";
        
        // When
        ErrorResponse response = new ErrorResponse(timestamp, status, error, message, path);
        
        // Then
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getPath()).isEqualTo(path);
    }
    
    @Test
    @DisplayName("Should create ErrorResponse with default constructor")
    void shouldCreateErrorResponseWithDefaultConstructor() {
        // When
        ErrorResponse response = new ErrorResponse();
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getStatus()).isEqualTo(0);
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getPath()).isNull();
    }
    
    @Test
    @DisplayName("Should set and get timestamp")
    void shouldSetAndGetTimestamp() {
        // Given
        ErrorResponse response = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        
        // When
        response.setTimestamp(timestamp);
        
        // Then
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }
    
    @Test
    @DisplayName("Should set and get status")
    void shouldSetAndGetStatus() {
        // Given
        ErrorResponse response = new ErrorResponse();
        
        // When
        response.setStatus(500);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(500);
    }
    
    @Test
    @DisplayName("Should set and get error")
    void shouldSetAndGetError() {
        // Given
        ErrorResponse response = new ErrorResponse();
        
        // When
        response.setError("Internal Server Error");
        
        // Then
        assertThat(response.getError()).isEqualTo("Internal Server Error");
    }
    
    @Test
    @DisplayName("Should set and get message")
    void shouldSetAndGetMessage() {
        // Given
        ErrorResponse response = new ErrorResponse();
        
        // When
        response.setMessage("An error occurred");
        
        // Then
        assertThat(response.getMessage()).isEqualTo("An error occurred");
    }
    
    @Test
    @DisplayName("Should set and get path")
    void shouldSetAndGetPath() {
        // Given
        ErrorResponse response = new ErrorResponse();
        
        // When
        response.setPath("/api/v1/beneficiaries");
        
        // Then
        assertThat(response.getPath()).isEqualTo("/api/v1/beneficiaries");
    }
    
    @Test
    @DisplayName("Should handle null values in setters")
    void shouldHandleNullValuesInSetters() {
        // Given
        ErrorResponse response = new ErrorResponse();
        
        // When
        response.setTimestamp(null);
        response.setError(null);
        response.setMessage(null);
        response.setPath(null);
        
        // Then
        assertThat(response.getTimestamp()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getPath()).isNull();
    }
    
    @Test
    @DisplayName("Should create error response for 404 error")
    void shouldCreateErrorResponseFor404Error() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ErrorResponse response = new ErrorResponse(
                now, 404, "Not Found", "Beneficiary not found", "/api/v1/beneficiaries/1"
        );
        
        // Then
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).contains("Beneficiary not found");
    }
    
    @Test
    @DisplayName("Should create error response for 409 error")
    void shouldCreateErrorResponseFor409Error() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ErrorResponse response = new ErrorResponse(
                now, 409, "Conflict", "Beneficiary already exists", "/api/v1/beneficiaries"
        );
        
        // Then
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getError()).isEqualTo("Conflict");
        assertThat(response.getMessage()).contains("already exists");
    }
    
    @Test
    @DisplayName("Should create error response for 422 error")
    void shouldCreateErrorResponseFor422Error() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ErrorResponse response = new ErrorResponse(
                now, 422, "Validation Failed", "Invalid account number", "/api/v1/beneficiaries"
        );
        
        // Then
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getError()).isEqualTo("Validation Failed");
    }
    
    @Test
    @DisplayName("Should create error response for 500 error")
    void shouldCreateErrorResponseFor500Error() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        ErrorResponse response = new ErrorResponse(
                now, 500, "Internal Server Error", "An unexpected error occurred", "/api/v1/beneficiaries"
        );
        
        // Then
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
    }
}
