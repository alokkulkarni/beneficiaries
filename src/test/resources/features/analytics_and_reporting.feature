Feature: Beneficiary Analytics and Reporting
  As a system administrator or business analyst
  I want to access analytics and reporting features
  So that I can monitor beneficiary usage, detect duplicates, and analyze trends

  Background:
    Given the beneficiary service is available

  Scenario: Get comprehensive analytics for customer with beneficiaries
    Given multiple beneficiaries exist for customer "CUST_ANALYTICS_001":
      | id  | customerId          | accountNumber | beneficiaryName    | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 100 | CUST_ANALYTICS_001 | ACC100        | John Smith         | 1111111111              | BANK001             | First Bank          | DOMESTIC        |
      | 101 | CUST_ANALYTICS_001 | ACC100        | Jane Doe           | 2222222222              | BANK002             | Second Bank         | INTERNATIONAL   |
      | 102 | CUST_ANALYTICS_001 | ACC101        | Bob Wilson         | 3333333333              | BANK001             | First Bank          | DOMESTIC        |
      | 103 | CUST_ANALYTICS_001 | ACC101        | Alice Brown        | 4444444444              | BANK003             | Third Bank          | DOMESTIC        |
    When I request analytics for customer "CUST_ANALYTICS_001"
    Then the response status should be 200
    And the analytics should show total beneficiaries is 4
    And the analytics should show active beneficiaries is 4
    And the analytics should include beneficiaries by type:
      | type          | count |
      | DOMESTIC      | 3     |
      | INTERNATIONAL | 1     |
    And the analytics should include beneficiaries by bank:
      | bankName    | count |
      | First Bank  | 2     |
      | Second Bank | 1     |
      | Third Bank  | 1     |
    And the analytics should include most recent beneficiary name

  Scenario: Get analytics for customer with no beneficiaries
    When I request analytics for customer "CUST_NO_BENEFICIARIES"
    Then the response status should be 200
    And the analytics should show total beneficiaries is 0
    And the analytics should show active beneficiaries is 0

  Scenario: Find potential duplicate beneficiaries with similar names
    Given multiple beneficiaries exist for customer "CUST_DUPLICATES_001":
      | id  | customerId           | accountNumber | beneficiaryName | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 200 | CUST_DUPLICATES_001 | ACC200        | John Smith      | 5555555555              | BANK001             | Bank One            | DOMESTIC        |
      | 201 | CUST_DUPLICATES_001 | ACC200        | John Smith      | 6666666666              | BANK002             | Bank Two            | DOMESTIC        |
      | 202 | CUST_DUPLICATES_001 | ACC201        | Jane Doe        | 7777777777              | BANK003             | Bank Three          | INTERNATIONAL   |
      | 203 | CUST_DUPLICATES_001 | ACC201        | Bob Wilson      | 8888888888              | BANK004             | Bank Four           | DOMESTIC        |
    When I request duplicate detection for customer "CUST_DUPLICATES_001"
    Then the response status should be 200
    And the duplicate results should contain at least 1 potential duplicate pair
    And the first duplicate pair should include beneficiary names "John Smith" and "John Smith"

  Scenario: Find duplicates when no similar names exist
    Given multiple beneficiaries exist for customer "CUST_NO_DUPLICATES":
      | id  | customerId          | accountNumber | beneficiaryName    | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 300 | CUST_NO_DUPLICATES | ACC300        | Alice Anderson     | 1010101010              | BANK001             | Bank Alpha          | DOMESTIC        |
      | 301 | CUST_NO_DUPLICATES | ACC300        | Bob Baker          | 2020202020              | BANK002             | Bank Beta           | DOMESTIC        |
      | 302 | CUST_NO_DUPLICATES | ACC301        | Charlie Chapman    | 3030303030              | BANK003             | Bank Gamma          | INTERNATIONAL   |
    When I request duplicate detection for customer "CUST_NO_DUPLICATES"
    Then the response status should be 200
    And the duplicate results should contain 0 potential duplicate pairs

  Scenario: Find duplicates for customer with no beneficiaries
    When I request duplicate detection for customer "CUST_EMPTY_DUPLICATES"
    Then the response status should be 200
    And the duplicate results should contain 0 potential duplicate pairs

  Scenario: Generate usage report for time period with new beneficiaries
    Given multiple beneficiaries exist for customer "CUST_USAGE_001":
      | id  | customerId      | accountNumber | beneficiaryName  | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 400 | CUST_USAGE_001 | ACC400        | Old Beneficiary  | 4040404040              | BANK001             | Bank One            | DOMESTIC        |
    When I request usage report for customer "CUST_USAGE_001" from "2025-01-01T00:00:00" to "2025-12-31T23:59:59"
    Then the response status should be 200
    And the usage report should show customerId "CUST_USAGE_001"
    And the usage report should include report period start "2025-01-01T00:00:00"
    And the usage report should include report period end "2025-12-31T23:59:59"
    And the usage report should show beneficiaries added in period

  Scenario: Generate usage report with no beneficiaries in period
    Given multiple beneficiaries exist for customer "CUST_USAGE_002":
      | id  | customerId      | accountNumber | beneficiaryName   | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 500 | CUST_USAGE_002 | ACC500        | Ancient Beneficiary | 5050505050            | BANK001             | Bank One            | DOMESTIC        |
    When I request usage report for customer "CUST_USAGE_002" from "2010-01-01T00:00:00" to "2010-12-31T23:59:59"
    Then the response status should be 200
    And the usage report should show beneficiaries added in period is 0
    And the usage report should include growth rate percent

  Scenario: Generate usage report for customer with no beneficiaries
    When I request usage report for customer "CUST_EMPTY_USAGE" from "2025-01-01T00:00:00" to "2025-12-31T23:59:59"
    Then the response status should be 200
    And the usage report should show total beneficiaries is 0
    And the usage report should show beneficiaries added in period is 0
    And the usage report should show growth rate percent is 0.0

  Scenario: Analytics should count only active beneficiaries
    Given a beneficiary exists with status:
      | id              | 600                |
      | customerId      | CUST_STATUS_CHECK |
      | accountNumber   | ACC600            |
      | beneficiaryName | Active User       |
      | status          | ACTIVE            |
    And a beneficiary exists with status:
      | id              | 601                |
      | customerId      | CUST_STATUS_CHECK |
      | accountNumber   | ACC601            |
      | beneficiaryName | Inactive User     |
      | status          | INACTIVE          |
    When I request analytics for customer "CUST_STATUS_CHECK"
    Then the response status should be 200
    And the analytics should show total beneficiaries is 2
    And the analytics should show active beneficiaries is 1
    And the analytics should show inactive beneficiaries is 1

  Scenario: Usage report should calculate growth rate correctly
    Given multiple beneficiaries exist for customer "CUST_GROWTH":
      | id  | customerId   | accountNumber | beneficiaryName | beneficiaryAccountNumber | beneficiaryBankCode | beneficiaryBankName | beneficiaryType |
      | 700 | CUST_GROWTH | ACC700        | User One        | 7070707070              | BANK001             | Bank One            | DOMESTIC        |
      | 701 | CUST_GROWTH | ACC701        | User Two        | 8080808080              | BANK002             | Bank Two            | DOMESTIC        |
      | 702 | CUST_GROWTH | ACC702        | User Three      | 9090909090              | BANK003             | Bank Three          | DOMESTIC        |
    When I request usage report for customer "CUST_GROWTH" from "2020-01-01T00:00:00" to "2030-12-31T23:59:59"
    Then the response status should be 200
    And the usage report should show total beneficiaries is 3
    And the usage report should show growth rate percent is greater than 0
