package com.alok.payment.beneficiaries.service;

import org.springframework.stereotype.Component;

/**
 * Mock client for third-party beneficiary validation service.
 * In production, this would call an external API for validation.
 */
@Component
public class ThirdPartyValidationClient {
    
    /**
     * Validates beneficiary details with external service.
     * This is a mock implementation that simulates third-party API responses.
     */
    public ThirdPartyValidationResponse validateBeneficiary(
            String accountNumber,
            String bankCode,
            String beneficiaryName,
            String beneficiaryType) {
        
        // Mock validation logic
        ThirdPartyValidationResponse response = new ThirdPartyValidationResponse();
        
        // Simulate different validation scenarios based on account number patterns
        if (accountNumber.startsWith("999")) {
            // Simulate sanctioned account
            response.setValid(false);
            response.setSanctioned(true);
            response.setFailureReason("Account is on sanctions list");
            return response;
        }
        
        if (accountNumber.startsWith("888")) {
            // Simulate high fraud risk
            response.setValid(true);
            response.setSanctioned(false);
            response.setFraudScore(0.85);
            response.setAccountStatus("ACTIVE");
            return response;
        }
        
        if (accountNumber.startsWith("777")) {
            // Simulate inactive account
            response.setValid(false);
            response.setSanctioned(false);
            response.setAccountStatus("CLOSED");
            response.setFailureReason("Account is closed");
            return response;
        }
        
        if (accountNumber.startsWith("666")) {
            // Simulate validation service error
            throw new RuntimeException("Third-party service temporarily unavailable");
        }
        
        // Default: valid account
        response.setValid(true);
        response.setSanctioned(false);
        response.setFraudScore(0.1);
        response.setAccountStatus("ACTIVE");
        
        return response;
    }
}
