Feature: Beneficiary Audit Service
  As a system administrator
  I want to track all beneficiary operations
  So that I can maintain an audit trail for compliance

  Background:
    Given the beneficiary audit service is available

  Scenario: Successfully audit beneficiary creation
    Given a beneficiary with ID 100 and customer "CUST_AUDIT_001" exists
    When I audit the creation of the beneficiary by "USER001"
    Then the audit record should be created successfully
    And the audit should have operation "CREATE"
    And the audit should have performer "USER001"
    And the audit changes should contain beneficiary details

  Scenario: Successfully audit beneficiary update
    Given a beneficiary with ID 101 and customer "CUST_AUDIT_002" exists
    When I audit the update of the beneficiary by "USER002"
    Then the audit record should be created successfully
    And the audit should have operation "UPDATE"
    And the audit should have performer "USER002"
    And the audit changes should contain beneficiary details

  Scenario: Successfully audit beneficiary deletion
    When I audit the deletion of beneficiary ID 102 for customer "CUST_AUDIT_003" by "USER003"
    Then the audit record should be created successfully
    And the audit should have operation "DELETE"
    And the audit should have performer "USER003"

  Scenario: Use SYSTEM as default performer when none specified
    Given a beneficiary with ID 103 and customer "CUST_AUDIT_004" exists
    When I audit the creation of the beneficiary with no performer specified
    Then the audit record should be created successfully
    And the audit should have performer "SYSTEM"

  Scenario: Retrieve audit history for a specific beneficiary
    Given a beneficiary with ID 104 and customer "CUST_AUDIT_005" exists
    And I audit the creation of the beneficiary by "USER001"
    And I audit the update of the beneficiary by "USER002"
    When I retrieve the audit history for beneficiary ID 104 and customer "CUST_AUDIT_005"
    Then I should receive 2 audit records
    And the audit records should contain operations "CREATE" and "UPDATE"

  Scenario: Retrieve audit history for a customer
    Given a beneficiary with ID 105 and customer "CUST_AUDIT_006" exists
    And another beneficiary with ID 106 and customer "CUST_AUDIT_006" exists
    And I audit the creation of beneficiary ID 105 by "USER001"
    And I audit the creation of beneficiary ID 106 by "USER002"
    And I audit the update of beneficiary ID 105 by "USER003"
    When I retrieve the audit history for customer "CUST_AUDIT_006"
    Then I should receive 3 audit records
    And the audit records should be ordered by performed date descending

  Scenario: Retrieve empty audit history for non-existent beneficiary
    When I retrieve the audit history for beneficiary ID 999 and customer "NONEXISTENT"
    Then I should receive 0 audit records

  Scenario: Audit changes should contain complete beneficiary information
    Given a beneficiary with ID 107 and customer "CUST_AUDIT_007" exists
    When I audit the creation of the beneficiary by "USER001"
    Then the audit changes should contain "CUST_AUDIT_007"
    And the audit changes should contain "BEN107"
    And the audit changes should contain "DOMESTIC"
