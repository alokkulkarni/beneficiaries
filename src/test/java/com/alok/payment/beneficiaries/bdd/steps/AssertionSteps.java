package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertionSteps {
    
    @Autowired
    private TestContext testContext;
    
    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        ResponseEntity<?> response = testContext.getLastResponse();
        Exception exception = testContext.getLastException();
        
        if (exception != null) {
            // Handle exception cases
            if (exception.getMessage().contains(String.valueOf(expectedStatus))) {
                return; // Expected error status
            }
            assertThat(exception).isNull();
        }
        
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
    }
    
    @Then("the error message should contain {string}")
    public void theErrorMessageShouldContain(String expectedMessage) {
        Exception exception = testContext.getLastException();
        if (exception != null) {
            assertThat(exception.getMessage()).contains(expectedMessage);
        } else {
            ResponseEntity<?> response = testContext.getLastResponse();
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode().is4xxClientError() || 
                      response.getStatusCode().is5xxServerError()).isTrue();
        }
    }
    
    @Then("the beneficiary should be retrieved successfully")
    public void theBeneficiaryShouldBeRetrievedSuccessfully() {
        ResponseEntity<?> response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getId()).isNotNull();
    }
    
    @Then("the beneficiary should be updated successfully")
    public void theBeneficiaryShouldBeUpdatedSuccessfully() {
        ResponseEntity<?> response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        assertThat(beneficiary.getId()).isNotNull();
    }
    
    @Then("the response should contain {int} beneficiaries")
    public void theResponseShouldContainBeneficiaries(int expectedCount) {
        ResponseEntity<?> response = testContext.getLastResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        List<BeneficiaryResponse> beneficiaries = testContext.getBeneficiaryList();
        assertThat(beneficiaries).isNotNull();
        assertThat(beneficiaries).hasSize(expectedCount);
    }

    @Then("the paged response should have totalElements {int}")
    public void thePagedResponseShouldHaveTotalElements(int totalElements) {
        PagedResponse<BeneficiaryResponse> paged = testContext.getPagedBeneficiaries();
        assertThat(paged).isNotNull();
        assertThat(paged.getTotalElements()).isEqualTo(totalElements);
    }

    @Then("the paged response page is {int} and size is {int}")
    public void thePagedResponseHasPageAndSize(int page, int size) {
        PagedResponse<BeneficiaryResponse> paged = testContext.getPagedBeneficiaries();
        assertThat(paged).isNotNull();
        assertThat(paged.getPage()).isEqualTo(page);
        assertThat(paged.getSize()).isEqualTo(size);
    }

    @Then("the first paged result beneficiaryName contains {string}")
    public void theFirstPagedResultBeneficiaryNameContains(String namePart) {
        PagedResponse<BeneficiaryResponse> paged = testContext.getPagedBeneficiaries();
        assertThat(paged).isNotNull();
        assertThat(paged.getContent()).isNotEmpty();
        assertThat(paged.getContent().get(0).getBeneficiaryName()).containsIgnoringCase(namePart);
    }
    
    @Then("the beneficiary should have the following details:")
    public void theBeneficiaryShouldHaveTheFollowingDetails(Map<String, String> expectedDetails) {
        BeneficiaryResponse beneficiary = testContext.getCurrentBeneficiary();
        assertThat(beneficiary).isNotNull();
        
        if (expectedDetails.containsKey("customerId")) {
            assertThat(beneficiary.getCustomerId()).isEqualTo(expectedDetails.get("customerId"));
        }
        if (expectedDetails.containsKey("beneficiaryName")) {
            assertThat(beneficiary.getBeneficiaryName()).isEqualTo(expectedDetails.get("beneficiaryName"));
        }
        if (expectedDetails.containsKey("beneficiaryBankName")) {
            assertThat(beneficiary.getBeneficiaryBankName()).isEqualTo(expectedDetails.get("beneficiaryBankName"));
        }
        if (expectedDetails.containsKey("beneficiaryType")) {
            assertThat(beneficiary.getBeneficiaryType()).isEqualTo(expectedDetails.get("beneficiaryType"));
        }
        if (expectedDetails.containsKey("accountNumber")) {
            assertThat(beneficiary.getAccountNumber()).isEqualTo(expectedDetails.get("accountNumber"));
        }
    }
    
    @Then("the beneficiaries should include:")
    public void theBeneficiariesShouldInclude(io.cucumber.datatable.DataTable dataTable) {
        List<BeneficiaryResponse> beneficiaries = testContext.getBeneficiaryList();
        assertThat(beneficiaries).isNotNull();
        assertThat(beneficiaries).isNotEmpty();
        
        List<Map<String, String>> expectedBeneficiaries = dataTable.asMaps();
        assertThat(beneficiaries).hasSizeGreaterThanOrEqualTo(expectedBeneficiaries.size());
        
        for (Map<String, String> expected : expectedBeneficiaries) {
            boolean found = beneficiaries.stream()
                .anyMatch(b -> matchesBeneficiary(b, expected));
            assertThat(found).isTrue();
        }
    }
    
    private boolean matchesBeneficiary(BeneficiaryResponse beneficiary, Map<String, String> expected) {
        if (expected.containsKey("beneficiaryName") && 
            !beneficiary.getBeneficiaryName().equals(expected.get("beneficiaryName"))) {
            return false;
        }
        if (expected.containsKey("beneficiaryType") && 
            !beneficiary.getBeneficiaryType().equals(expected.get("beneficiaryType"))) {
            return false;
        }
        if (expected.containsKey("accountNumber") && 
            !beneficiary.getAccountNumber().equals(expected.get("accountNumber"))) {
            return false;
        }
        return true;
    }
}
