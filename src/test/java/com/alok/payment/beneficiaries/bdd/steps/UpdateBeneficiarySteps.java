package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class UpdateBeneficiarySteps {
    
    @Autowired
    private CucumberSpringConfiguration springConfiguration;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TestContext testContext;
    
    @When("I update beneficiary {long} for customer {string} with:")
    public void iUpdateBeneficiaryForCustomerWith(Long id, String customerId, Map<String, String> updates) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(customerId);
        request.setAccountNumber("ACC001");
        request.setBeneficiaryName(updates.getOrDefault("beneficiaryName", "Original Name"));
        request.setBeneficiaryAccountNumber("BEN001");
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName(updates.getOrDefault("beneficiaryBankName", "Original Bank"));
        request.setBeneficiaryType(updates.getOrDefault("beneficiaryType", "DOMESTIC"));
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/" + id + "?customerId=" + customerId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<BeneficiaryResponse> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, BeneficiaryResponse.class);
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setCurrentBeneficiary(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I update beneficiary {long} for customer {string} with invalid type {string}")
    public void iUpdateBeneficiaryForCustomerWithInvalidType(Long id, String customerId, String invalidType) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(customerId);
        request.setAccountNumber("ACC001");
        request.setBeneficiaryName("Test Name");
        request.setBeneficiaryAccountNumber("BEN001");
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName("Test Bank");
        request.setBeneficiaryType(invalidType);
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/" + id + "?customerId=" + customerId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I update beneficiary {long} for customer {string} with null beneficiary type")
    public void iUpdateBeneficiaryForCustomerWithNullBeneficiaryType(Long id, String customerId) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(customerId);
        request.setAccountNumber("ACC001");
        request.setBeneficiaryName("Updated Name");
        request.setBeneficiaryAccountNumber("BEN001");
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName("Test Bank");
        request.setBeneficiaryType(null);  // Null type
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/" + id + "?customerId=" + customerId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<BeneficiaryResponse> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, BeneficiaryResponse.class);
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setCurrentBeneficiary(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
}
