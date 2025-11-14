package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateBeneficiarySteps {
    
    @Autowired
    private CucumberSpringConfiguration springConfiguration;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TestContext testContext;
    
    @When("I create a beneficiary with the following details:")
    public void iCreateABeneficiaryWithTheFollowingDetails(Map<String, String> details) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(details.get("customerId"));
        request.setAccountNumber(details.get("accountNumber"));
        request.setBeneficiaryName(details.get("beneficiaryName"));
        request.setBeneficiaryAccountNumber(details.get("beneficiaryAccountNumber"));
        request.setBeneficiaryBankCode(details.get("beneficiaryBankCode"));
        request.setBeneficiaryBankName(details.get("beneficiaryBankName"));
        request.setBeneficiaryType(details.get("beneficiaryType"));
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<BeneficiaryResponse> response = restTemplate.postForEntity(url, entity, BeneficiaryResponse.class);
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setCurrentBeneficiary(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I create a beneficiary with missing customerId")
    public void iCreateABeneficiaryWithMissingCustomerId() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId("");  // Empty customer ID
        request.setAccountNumber("ACC001");
        request.setBeneficiaryName("John Doe");
        request.setBeneficiaryAccountNumber("BEN001");
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName("Test Bank");
        request.setBeneficiaryType("DOMESTIC");
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I create a beneficiary with invalid type {string}")
    public void iCreateABeneficiaryWithInvalidType(String invalidType) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId("CUST001");
        request.setAccountNumber("ACC001");
        request.setBeneficiaryName("John Doe");
        request.setBeneficiaryAccountNumber("BEN001");
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName("Test Bank");
        request.setBeneficiaryType(invalidType);
        
        testContext.setCurrentRequest(request);
        
        String url = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I create a beneficiary with the same details")
    public void iCreateABeneficiaryWithTheSameDetails() {
        BeneficiaryRequest request = testContext.getCurrentRequest();
        
        String url = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BeneficiaryRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @Then("the beneficiary should be created successfully")
    public void theBeneficiaryShouldBeCreatedSuccessfully() {
        ResponseEntity<?> response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getId()).isNotNull();
    }
}
