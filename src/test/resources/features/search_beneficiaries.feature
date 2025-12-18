Feature: Search beneficiaries with filters and pagination

  Background:
    Given the beneficiary service is available

  Scenario: Search by name with pagination and sorting
    Given multiple beneficiaries exist for customer "CUST_ANALYTICS":
      | id | customerId     | accountNumber | beneficiaryName | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 50 | CUST_ANALYTICS | ACC           | John Doe        | 111222333               | BANK010             | Analytics Bank      | DOMESTIC        |
    When I search beneficiaries with criteria:
      | customerId   | CUST_ANALYTICS |
      | beneficiaryName | John        |
      | page         | 0             |
      | size         | 2             |
      | sortBy       | beneficiaryName |
      | sortDirection| ASC           |
    Then the response status should be 200
    And the paged response page is 0 and size is 2
    And the first paged result beneficiaryName contains "John"

  Scenario: Search by type and status
    When I search beneficiaries with criteria:
      | customerId      | CUST_ANALYTICS |
      | beneficiaryType | DOMESTIC       |
      | status          | ACTIVE         |
      | page            | 0              |
      | size            | 10             |
    Then the response status should be 200

  Scenario: Search by bank code and date range
    When I search beneficiaries with criteria:
      | customerId         | CUST_ANALYTICS |
      | beneficiaryBankCode| BANK011        |
      | createdAfter       | 2020-01-01T00:00:00 |
      | createdBefore      | 2100-01-01T00:00:00 |
      | page               | 0              |
      | size               | 5              |
    Then the response status should be 200