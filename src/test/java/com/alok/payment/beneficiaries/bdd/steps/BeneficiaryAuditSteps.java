package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.model.BeneficiaryAudit;
import com.alok.payment.beneficiaries.service.BeneficiaryAuditService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeneficiaryAuditSteps {
    
    @Autowired
    private TestContext testContext;
    
    @Autowired
    private BeneficiaryAuditService auditService;
    
    private Beneficiary currentBeneficiary;
    private Beneficiary anotherBeneficiary;
    private BeneficiaryAudit currentAudit;
    private List<BeneficiaryAudit> auditHistory;
    
    @Given("the beneficiary audit service is available")
    public void theBeneficiaryAuditServiceIsAvailable() {
        assertThat(auditService).isNotNull();
    }
    
    @Given("a beneficiary with ID {long} and customer {string} exists")
    public void aBeneficiaryWithIDAndCustomerExists(Long id, String customerId) {
        currentBeneficiary = createTestBeneficiary(id, customerId, "Test User " + id);
    }
    
    @Given("another beneficiary with ID {long} and customer {string} exists")
    public void anotherBeneficiaryWithIDAndCustomerExists(Long id, String customerId) {
        anotherBeneficiary = createTestBeneficiary(id, customerId, "Another User " + id);
    }
    
    @When("I audit the creation of the beneficiary by {string}")
    public void iAuditTheCreationOfTheBeneficiaryBy(String performer) {
        currentAudit = auditService.auditCreate(currentBeneficiary, performer);
    }
    
    @When("I audit the creation of the beneficiary with no performer specified")
    public void iAuditTheCreationOfTheBeneficiaryWithNoPerformerSpecified() {
        currentAudit = auditService.auditCreate(currentBeneficiary, null);
    }
    
    @When("I audit the update of the beneficiary by {string}")
    public void iAuditTheUpdateOfTheBeneficiaryBy(String performer) {
        currentAudit = auditService.auditUpdate(currentBeneficiary, performer);
    }
    
    @When("I audit the deletion of beneficiary ID {long} for customer {string} by {string}")
    public void iAuditTheDeletionOfBeneficiaryIDForCustomerBy(Long beneficiaryId, String customerId, String performer) {
        currentAudit = auditService.auditDelete(beneficiaryId, customerId, performer);
    }
    
    @When("I retrieve the audit history for beneficiary ID {long} and customer {string}")
    public void iRetrieveTheAuditHistoryForBeneficiaryIDAndCustomer(Long beneficiaryId, String customerId) {
        auditHistory = auditService.getAuditHistory(beneficiaryId, customerId);
    }
    
    @When("I retrieve the audit history for customer {string}")
    public void iRetrieveTheAuditHistoryForCustomer(String customerId) {
        auditHistory = auditService.getCustomerAuditHistory(customerId);
    }
    
    @Then("the audit record should be created successfully")
    public void theAuditRecordShouldBeCreatedSuccessfully() {
        assertThat(currentAudit).isNotNull();
        assertThat(currentAudit.getId()).isNotNull();
        assertThat(currentAudit.getPerformedAt()).isNotNull();
    }
    
    @Then("the audit should have operation {string}")
    public void theAuditShouldHaveOperation(String operation) {
        assertThat(currentAudit.getOperation()).isEqualTo(operation);
    }
    
    @Then("the audit should have performer {string}")
    public void theAuditShouldHavePerformer(String performer) {
        assertThat(currentAudit.getPerformedBy()).isEqualTo(performer);
    }
    
    @Then("the audit changes should contain beneficiary details")
    public void theAuditChangesShouldContainBeneficiaryDetails() {
        assertThat(currentAudit.getChanges()).isNotNull();
        assertThat(currentAudit.getChanges()).isNotBlank();
    }
    
    @Then("the audit changes should contain {string}")
    public void theAuditChangesShouldContain(String content) {
        assertThat(currentAudit.getChanges()).contains(content);
    }
    
    @Then("I should receive {int} audit records")
    public void iShouldReceiveAuditRecords(int count) {
        assertThat(auditHistory).hasSize(count);
    }
    
    @Then("the audit records should contain operations {string} and {string}")
    public void theAuditRecordsShouldContainOperationsAnd(String operation1, String operation2) {
        assertThat(auditHistory)
                .extracting(BeneficiaryAudit::getOperation)
                .containsExactlyInAnyOrder(operation1, operation2);
    }
    
    @Then("the audit records should be ordered by performed date descending")
    public void theAuditRecordsShouldBeOrderedByPerformedDateDescending() {
        if (auditHistory.size() > 1) {
            for (int i = 0; i < auditHistory.size() - 1; i++) {
                LocalDateTime current = auditHistory.get(i).getPerformedAt();
                LocalDateTime next = auditHistory.get(i + 1).getPerformedAt();
                assertThat(current).isAfterOrEqualTo(next);
            }
        }
    }
    
    @And("I audit the creation of beneficiary ID {long} by {string}")
    public void iAuditTheCreationOfBeneficiaryIDBy(Long beneficiaryId, String performer) {
        Beneficiary beneficiary = (beneficiaryId.equals(currentBeneficiary.getId())) 
                ? currentBeneficiary 
                : anotherBeneficiary;
        auditService.auditCreate(beneficiary, performer);
    }
    
    @And("I audit the update of beneficiary ID {long} by {string}")
    public void iAuditTheUpdateOfBeneficiaryIDBy(Long beneficiaryId, String performer) {
        Beneficiary beneficiary = (beneficiaryId.equals(currentBeneficiary.getId())) 
                ? currentBeneficiary 
                : anotherBeneficiary;
        auditService.auditUpdate(beneficiary, performer);
    }
    
    private Beneficiary createTestBeneficiary(Long id, String customerId, String name) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(id);
        beneficiary.setCustomerId(customerId);
        beneficiary.setAccountNumber("ACC" + id);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setBeneficiaryAccountNumber("BEN" + id);
        beneficiary.setBeneficiaryBankCode("BANK001");
        beneficiary.setBeneficiaryBankName("Test Bank");
        beneficiary.setBeneficiaryType("DOMESTIC");
        beneficiary.setStatus("ACTIVE");
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        return beneficiary;
    }
}
