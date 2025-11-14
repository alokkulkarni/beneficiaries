Feature: Delete Beneficiary
  As a customer
  I want to delete a beneficiary
  So that I can remove outdated or incorrect beneficiaries

  Background:
    Given the beneficiary service is available

  Scenario: Successfully delete an existing beneficiary
    Given a beneficiary exists with:
      | id                      | 2              |
      | customerId              | CUST006        |
      | accountNumber           | ACC006         |
      | beneficiaryName         | Jane Smith     |
    When I delete beneficiary 2 for customer "CUST006"
    Then the beneficiary should be deleted successfully
    And the response status should be 204

  Scenario: Fail to delete non-existent beneficiary
    When I delete beneficiary 999 for customer "CUST001"
    Then the response status should be 404
    And the error message should contain "Beneficiary not found"

  Scenario: Fail to delete beneficiary with wrong customer ID
    Given a beneficiary exists with:
      | id                      | 3              |
      | customerId              | CUST007        |
      | accountNumber           | ACC007         |
      | beneficiaryName         | Bob Johnson    |
    When I delete beneficiary 3 for customer "WRONG_CUSTOMER"
    Then the response status should be 404
    And the error message should contain "Beneficiary not found"

  Scenario: Soft delete does not permanently remove beneficiary
    Given a beneficiary exists with:
      | id                      | 4              |
      | customerId              | CUST003        |
      | accountNumber           | ACC003         |
      | beneficiaryName         | Deleted User   |
    When I delete beneficiary 4 for customer "CUST003"
    Then the beneficiary should be soft deleted
    And the beneficiary status should be "DELETED"
