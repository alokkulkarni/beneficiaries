Feature: Update Beneficiary
  As a customer
  I want to update an existing beneficiary
  So that I can keep beneficiary information current

  Background:
    Given the beneficiary service is available
    And a beneficiary exists with:
      | id                      | 1              |
      | customerId              | CUST001        |
      | accountNumber           | ACC001         |
      | beneficiaryName         | Old Name       |
      | beneficiaryAccountNumber| BEN001         |
      | beneficiaryBankCode     | BANK001        |
      | beneficiaryBankName     | Old Bank       |
      | beneficiaryType         | DOMESTIC       |

  Scenario: Successfully update beneficiary details
    When I update beneficiary 1 for customer "CUST001" with:
      | beneficiaryName         | Updated Name   |
      | beneficiaryBankName     | Updated Bank   |
    Then the beneficiary should be updated successfully
    And the response status should be 200
    And the beneficiary should have the following details:
      | beneficiaryName         | Updated Name   |
      | beneficiaryBankName     | Updated Bank   |

  Scenario: Successfully change beneficiary type
    When I update beneficiary 1 for customer "CUST001" with:
      | beneficiaryType         | INTERNATIONAL  |
    Then the beneficiary should be updated successfully
    And the response status should be 200
    And the beneficiary should have the following details:
      | beneficiaryType         | INTERNATIONAL  |

  Scenario: Fail to update non-existent beneficiary
    When I update beneficiary 999 for customer "CUST001" with:
      | beneficiaryName         | New Name       |
    Then the response status should be 404
    And the error message should contain "Beneficiary not found"

  Scenario: Fail to update beneficiary with invalid data
    When I update beneficiary 1 for customer "CUST001" with invalid type "INVALID"
    Then the response status should be 400
    And the error message should contain "Beneficiary type must be DOMESTIC or INTERNATIONAL"

  Scenario: Update beneficiary preserves type when null
    When I update beneficiary 1 for customer "CUST001" with null beneficiary type
    Then the beneficiary should be updated successfully
    And the beneficiary should have the following details:
      | beneficiaryType         | DOMESTIC       |
