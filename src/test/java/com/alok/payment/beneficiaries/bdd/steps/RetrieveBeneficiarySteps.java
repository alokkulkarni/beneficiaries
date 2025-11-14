package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class RetrieveBeneficiarySteps {
    
    @Autowired
    private CucumberSpringConfiguration springConfiguration;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TestContext testContext;
    
    @When("I retrieve beneficiary {long} for customer {string}")
    public void iRetrieveBeneficiaryForCustomer(Long id, String customerId) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/" + id + "?customerId=" + customerId;
        
        try {
            ResponseEntity<BeneficiaryResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, null, BeneficiaryResponse.class);
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setCurrentBeneficiary(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I retrieve all beneficiaries for customer {string}")
    public void iRetrieveAllBeneficiariesForCustomer(String customerId) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries?customerId=" + customerId;
        
        try {
            ResponseEntity<List<BeneficiaryResponse>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, 
                new ParameterizedTypeReference<List<BeneficiaryResponse>>() {});
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setBeneficiaryList(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I retrieve beneficiaries for customer {string} and account {string}")
    public void iRetrieveBeneficiariesForCustomerAndAccount(String customerId, String accountNumber) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries?customerId=" + customerId + 
                    "&accountNumber=" + accountNumber;
        
        try {
            ResponseEntity<List<BeneficiaryResponse>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, 
                new ParameterizedTypeReference<List<BeneficiaryResponse>>() {});
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setBeneficiaryList(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @When("I retrieve beneficiaries for customer {string} with null account number")
    public void iRetrieveBeneficiariesForCustomerWithNullAccountNumber(String customerId) {
        // Same as retrieving all beneficiaries - null is not sent
        iRetrieveAllBeneficiariesForCustomer(customerId);
    }
    
    @When("I retrieve beneficiaries for customer {string} with empty account number")
    public void iRetrieveBeneficiariesForCustomerWithEmptyAccountNumber(String customerId) {
        // Empty string treated as all beneficiaries
        iRetrieveAllBeneficiariesForCustomer(customerId);
    }
}
