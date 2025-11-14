Feature: Retrieve Beneficiaries
  As a customer
  I want to retrieve beneficiaries
  So that I can view and select beneficiaries for payments

  Background:
    Given the beneficiary service is available

  Scenario: Successfully retrieve beneficiary by ID
    Given a beneficiary exists with:
      | id                      | 1              |
      | customerId              | CUST001        |
      | accountNumber           | ACC001         |
      | beneficiaryName         | Old Name       |
      | beneficiaryType         | DOMESTIC       |
    When I retrieve beneficiary 1 for customer "CUST001"
    Then the beneficiary should be retrieved successfully
    And the response status should be 200
    And the beneficiary should have the following details:
      | id                      | 1              |
      | customerId              | CUST001        |
      | beneficiaryName         | Old Name       |
      | beneficiaryType         | DOMESTIC       |

  Scenario: Fail to retrieve non-existent beneficiary
    When I retrieve beneficiary 999 for customer "CUST001"
    Then the response status should be 404
    And the error message should contain "Beneficiary not found"

  Scenario: Successfully retrieve all beneficiaries for a customer
    Given multiple beneficiaries exist for customer "CUST002":
      | id | beneficiaryName | accountNumber |
      | 10 | Beneficiary One | ACC010        |
      | 11 | Beneficiary Two | ACC011        |
      | 12 | Beneficiary Three | ACC012      |
    When I retrieve all beneficiaries for customer "CUST002"
    Then the response status should be 200
    And the response should contain 3 beneficiaries
    And the beneficiaries should include:
      | beneficiaryName         |
      | Beneficiary One         |
      | Beneficiary Two         |
      | Beneficiary Three       |

  Scenario: Successfully retrieve beneficiaries by customer and account number
    Given multiple beneficiaries exist for customer "CUST003":
      | id | beneficiaryName | accountNumber |
      | 20 | Account A User  | ACC_A         |
      | 21 | Account B User  | ACC_B         |
      | 22 | Another A User  | ACC_A         |
    When I retrieve beneficiaries for customer "CUST003" and account "ACC_A"
    Then the response status should be 200
    And the response should contain 2 beneficiaries
    And the beneficiaries should include:
      | beneficiaryName         |
      | Account A User          |
      | Another A User          |

  Scenario: Retrieve beneficiaries with null account number returns all
    Given multiple beneficiaries exist for customer "CUST004":
      | id | beneficiaryName | accountNumber |
      | 30 | User One        | ACC030        |
      | 31 | User Two        | ACC031        |
    When I retrieve beneficiaries for customer "CUST004" with null account number
    Then the response status should be 200
    And the response should contain 2 beneficiaries

  Scenario: Retrieve beneficiaries with empty account number returns all
    Given multiple beneficiaries exist for customer "CUST005":
      | id | beneficiaryName | accountNumber |
      | 40 | User Alpha      | ACC040        |
      | 41 | User Beta       | ACC041        |
    When I retrieve beneficiaries for customer "CUST005" with empty account number
    Then the response status should be 200
    And the response should contain 2 beneficiaries

  Scenario: Empty result when customer has no beneficiaries
    When I retrieve all beneficiaries for customer "NONEXISTENT"
    Then the response status should be 200
    And the response should contain 0 beneficiaries
