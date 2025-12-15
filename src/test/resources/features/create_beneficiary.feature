Feature: Create Beneficiary
  As a customer
  I want to create a new beneficiary
  So that I can send payments to them

  Background:
    Given the beneficiary service is available

  Scenario: Successfully create a domestic beneficiary
    When I create a beneficiary with the following details:
      | customerId              | CUST999        |
      | accountNumber           | ACC999         |
      | beneficiaryName         | John Doe       |
      | beneficiaryAccountNumber| 11111111       |
      | beneficiaryBankCode     | BANK001        |
      | beneficiaryBankName     | Test Bank      |
      | beneficiaryType         | DOMESTIC       |
    Then the beneficiary should be created successfully
    And the response status should be 201
    And the beneficiary should have the following details:
      | customerId              | CUST999        |
      | beneficiaryName         | John Doe       |
      | beneficiaryType         | DOMESTIC       |

  Scenario: Successfully create an international beneficiary
    When I create a beneficiary with the following details:
      | customerId              | CUST002        |
      | accountNumber           | ACC002         |
      | beneficiaryName         | Jane Smith     |
      | beneficiaryAccountNumber| 12345678       |
      | beneficiaryBankCode     | SWIFT001       |
      | beneficiaryBankName     | Global Bank    |
      | beneficiaryType         | INTERNATIONAL  |
    Then the beneficiary should be created successfully
    And the response status should be 201
    And the beneficiary should have the following details:
      | beneficiaryName         | Jane Smith     |
      | beneficiaryType         | INTERNATIONAL  |

  Scenario: Fail to create beneficiary with missing required fields
    When I create a beneficiary with missing customerId
    Then the response status should be 400
    And the error message should contain "Customer ID is required"

  Scenario: Fail to create beneficiary with invalid beneficiary type
    When I create a beneficiary with invalid type "INVALID"
    Then the response status should be 400
    And the error message should contain "Beneficiary type must be DOMESTIC or INTERNATIONAL"

  Scenario: Fail to create duplicate beneficiary
    Given a beneficiary already exists with:
      | id                      | 5              |
      | customerId              | CUST003        |
      | accountNumber           | ACC003         |
      | beneficiaryAccountNumber| 12341234       |
    When I create a beneficiary with the same details
    Then the response status should be 409
    And the error message should contain "Beneficiary already exists"
