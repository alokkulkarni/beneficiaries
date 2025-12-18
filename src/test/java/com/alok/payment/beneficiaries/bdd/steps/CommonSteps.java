package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class CommonSteps {
    
    @Autowired
    private CucumberSpringConfiguration springConfiguration;
    
    @Autowired
    private TestContext testContext;
    
    @Autowired
    private BeneficiaryRepository beneficiaryRepository;
    
            @Before
    public void setUp() {
        testContext.reset();
        // Don't clear database - use data from init.db
    }
    
    @Given("the beneficiary service is available")
    public void theBeneficiaryServiceIsAvailable() {
        String baseUrl = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries";
        testContext.setTestData("baseUrl", baseUrl);
    }
    
    @Given("a beneficiary exists with:")
    public void aBeneficiaryExistsWithDomesticTypeForCustomer(Map<String, String> beneficiaryDetails) {
        // Use pre-existing data from init.db - just fetch it
        Long beneficiaryId = Long.parseLong(beneficiaryDetails.get("id"));
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found in init.db with id: " + beneficiaryId));
        testContext.setTestData("existingBeneficiaryId", beneficiary.getId());
        testContext.setCurrentBeneficiary(mapToResponse(beneficiary));
    }
    
    @Given("a beneficiary already exists with:")
    public void aBeneficiaryAlreadyExistsWith(Map<String, String> beneficiaryData) {
        aBeneficiaryExistsWithDomesticTypeForCustomer(beneficiaryData);
        // Store the request data for duplicate creation attempt
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(beneficiaryData.get("customerId"));
        request.setAccountNumber(beneficiaryData.get("accountNumber"));
        request.setBeneficiaryName("Duplicate Name");
        request.setBeneficiaryAccountNumber(beneficiaryData.get("beneficiaryAccountNumber"));
        request.setBeneficiaryBankCode("BANK001");
        request.setBeneficiaryBankName("Test Bank");
        request.setBeneficiaryType("DOMESTIC");
        testContext.setCurrentRequest(request);
    }
    
    @Given("multiple beneficiaries exist for customer {string}:")
    public void multipleBeneficiariesExistForCustomer(String customerId, List<Map<String, String>> beneficiaries) {
        // Data already exists in init.db - no need to create
        // Just verify it exists
        List<Beneficiary> existingBeneficiaries = beneficiaryRepository.findByCustomerId(customerId);
        if (existingBeneficiaries.isEmpty()) {
            throw new RuntimeException("No beneficiaries found for customer " + customerId + " in init.db");
        }
    }
    
    @Given("a beneficiary exists with status:")
    public void aBeneficiaryExistsWithStatus(Map<String, String> beneficiaryDetails) {
        // Use pre-existing data from init.db - just fetch it
        Long beneficiaryId = Long.parseLong(beneficiaryDetails.get("id"));
        Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found in init.db with id: " + beneficiaryId));
        testContext.setTestData("existingBeneficiaryId", beneficiary.getId());
        testContext.setCurrentBeneficiary(mapToResponse(beneficiary));
    }
    
    private BeneficiaryResponse mapToResponse(Beneficiary beneficiary) {
        BeneficiaryResponse response = new BeneficiaryResponse();
        response.setId(beneficiary.getId());
        response.setCustomerId(beneficiary.getCustomerId());
        response.setAccountNumber(beneficiary.getAccountNumber());
        response.setBeneficiaryName(beneficiary.getBeneficiaryName());
        response.setBeneficiaryAccountNumber(beneficiary.getBeneficiaryAccountNumber());
        response.setBeneficiaryBankCode(beneficiary.getBeneficiaryBankCode());
        response.setBeneficiaryBankName(beneficiary.getBeneficiaryBankName());
        response.setBeneficiaryType(beneficiary.getBeneficiaryType());
        response.setStatus(beneficiary.getStatus());
        response.setCreatedAt(beneficiary.getCreatedAt());
        response.setUpdatedAt(beneficiary.getUpdatedAt());
        return response;
    }
}
