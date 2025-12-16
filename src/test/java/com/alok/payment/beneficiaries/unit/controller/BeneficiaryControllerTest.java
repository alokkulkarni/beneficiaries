package com.alok.payment.beneficiaries.unit.controller;

import com.alok.payment.beneficiaries.controller.BeneficiaryController;
import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryNotFoundException;
import com.alok.payment.beneficiaries.exception.DuplicateBeneficiaryException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeneficiaryController.class)
@DisplayName("BeneficiaryController Unit Tests")
class BeneficiaryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
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
    void shouldCreateBeneficiarySuccessfully() throws Exception {
        // Given
        when(beneficiaryService.createBeneficiary(any(BeneficiaryRequest.class)))
                .thenReturn(beneficiary);
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value("CUST001"))
                .andExpect(jsonPath("$.beneficiaryName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        
        verify(beneficiaryService).createBeneficiary(any(BeneficiaryRequest.class));
    }
    
    @Test
    @DisplayName("Should return 400 when request has validation errors")
    void shouldReturn400WhenRequestHasValidationErrors() throws Exception {
        // Given - Invalid request with missing required fields
        String invalidRequest = """
                {
                    "customerId": "",
                    "accountNumber": "ACC001"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
        
        verify(beneficiaryService, never()).createBeneficiary(any());
    }
    
    @Test
    @DisplayName("Should return 400 when beneficiary type is invalid")
    void shouldReturn400WhenBeneficiaryTypeIsInvalid() throws Exception {
        // Given
        String invalidRequest = """
                {
                    "customerId": "CUST001",
                    "accountNumber": "ACC001",
                    "beneficiaryName": "Test",
                    "beneficiaryAccountNumber": "BEN001",
                    "beneficiaryBankCode": "BANK001",
                    "beneficiaryBankName": "Test Bank",
                    "beneficiaryType": "INVALID"
                }
                """;
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
        
        verify(beneficiaryService, never()).createBeneficiary(any());
    }
    
    @Test
    @DisplayName("Should return 409 when creating duplicate beneficiary")
    void shouldReturn409WhenCreatingDuplicateBeneficiary() throws Exception {
        // Given
        when(beneficiaryService.createBeneficiary(any(BeneficiaryRequest.class)))
                .thenThrow(new DuplicateBeneficiaryException("Beneficiary already exists"));
        
        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
        
        verify(beneficiaryService).createBeneficiary(any(BeneficiaryRequest.class));
    }
    
    @Test
    @DisplayName("Should update beneficiary successfully")
    void shouldUpdateBeneficiarySuccessfully() throws Exception {
        // Given
        when(beneficiaryService.updateBeneficiary(eq(1L), eq("CUST001"), any(BeneficiaryRequest.class)))
                .thenReturn(beneficiary);
        
        // When & Then
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.beneficiaryName").value("John Doe"));
        
        verify(beneficiaryService).updateBeneficiary(eq(1L), eq("CUST001"), any(BeneficiaryRequest.class));
    }
    
    @Test
    @DisplayName("Should return 404 when updating non-existent beneficiary")
    void shouldReturn404WhenUpdatingNonExistentBeneficiary() throws Exception {
        // Given
        when(beneficiaryService.updateBeneficiary(eq(1L), eq("CUST001"), any(BeneficiaryRequest.class)))
                .thenThrow(new BeneficiaryNotFoundException("Beneficiary not found"));
        
        // When & Then
        mockMvc.perform(put("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
        
        verify(beneficiaryService).updateBeneficiary(eq(1L), eq("CUST001"), any(BeneficiaryRequest.class));
    }
    
    @Test
    @DisplayName("Should delete beneficiary successfully")
    void shouldDeleteBeneficiarySuccessfully() throws Exception {
        // Given
        doNothing().when(beneficiaryService).deleteBeneficiary(1L, "CUST001");
        
        // When & Then
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNoContent());
        
        verify(beneficiaryService).deleteBeneficiary(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should return 404 when deleting non-existent beneficiary")
    void shouldReturn404WhenDeletingNonExistentBeneficiary() throws Exception {
        // Given
        doThrow(new BeneficiaryNotFoundException("Beneficiary not found"))
                .when(beneficiaryService).deleteBeneficiary(1L, "CUST001");
        
        // When & Then
        mockMvc.perform(delete("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
        
        verify(beneficiaryService).deleteBeneficiary(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should get beneficiary by ID successfully")
    void shouldGetBeneficiaryByIdSuccessfully() throws Exception {
        // Given
        when(beneficiaryService.getBeneficiary(1L, "CUST001"))
                .thenReturn(beneficiary);
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.beneficiaryName").value("John Doe"));
        
        verify(beneficiaryService).getBeneficiary(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should return 404 when getting non-existent beneficiary")
    void shouldReturn404WhenGettingNonExistentBeneficiary() throws Exception {
        // Given
        when(beneficiaryService.getBeneficiary(1L, "CUST001"))
                .thenThrow(new BeneficiaryNotFoundException("Beneficiary not found"));
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries/{id}", 1L)
                        .param("customerId", "CUST001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
        
        verify(beneficiaryService).getBeneficiary(1L, "CUST001");
    }
    
    @Test
    @DisplayName("Should get all beneficiaries for customer")
    void shouldGetAllBeneficiariesForCustomer() throws Exception {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary, beneficiary);
        when(beneficiaryService.getBeneficiaries("CUST001", null))
                .thenReturn(beneficiaries);
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(beneficiaryService).getBeneficiaries("CUST001", null);
    }
    
    @Test
    @DisplayName("Should get beneficiaries by customer and account number")
    void shouldGetBeneficiariesByCustomerAndAccountNumber() throws Exception {
        // Given
        List<Beneficiary> beneficiaries = Arrays.asList(beneficiary);
        when(beneficiaryService.getBeneficiaries("CUST001", "ACC001"))
                .thenReturn(beneficiaries);
        
        // When & Then
        mockMvc.perform(get("/api/v1/beneficiaries")
                        .param("customerId", "CUST001")
                        .param("accountNumber", "ACC001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC001"));
        
        verify(beneficiaryService).getBeneficiaries("CUST001", "ACC001");
    }
}
