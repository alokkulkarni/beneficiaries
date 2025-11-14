package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBeneficiarySteps {
    
    @Autowired
    private CucumberSpringConfiguration springConfiguration;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TestContext testContext;
    
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    
    @When("I delete beneficiary {long} for customer {string}")
    public void iDeleteBeneficiaryForCustomer(Long id, String customerId) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/" + id + "?customerId=" + customerId;
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, null, Void.class);
            testContext.setLastResponse(response);
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
    
    @Then("the beneficiary should be deleted successfully")
    public void theBeneficiaryShouldBeDeletedSuccessfully() {
        ResponseEntity<?> response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
    
    @Then("the beneficiary should be soft deleted")
    public void theBeneficiaryShouldBeSoftDeleted() {
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        
        // Verify in database
        Optional<Beneficiary> dbBeneficiary = beneficiaryRepository.findById(beneficiary.getId());
        assertThat(dbBeneficiary).isPresent();
        assertThat(dbBeneficiary.get().getStatus()).isEqualTo("DELETED");
    }
    
    @Then("the beneficiary status should be {string}")
    public void theBeneficiaryStatusShouldBe(String expectedStatus) {
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        
        // Verify in database
        Optional<Beneficiary> dbBeneficiary = beneficiaryRepository.findById(beneficiary.getId());
        assertThat(dbBeneficiary).isPresent();
        assertThat(dbBeneficiary.get().getStatus()).isEqualTo(expectedStatus);
    }
}
