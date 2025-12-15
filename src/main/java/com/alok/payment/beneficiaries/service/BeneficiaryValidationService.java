package com.alok.payment.beneficiaries.service;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service for validating beneficiaries against third-party services.
 * Performs account validation, fraud checks, and sanctions screening.
 */
@Service
public class BeneficiaryValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryValidationService.class);
    
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{8,20}$");
    private static final Pattern BANK_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6,11}$");
    
    @Value("${beneficiary.validation.enabled:true}")
    private boolean validationEnabled;
    
    @Value("${beneficiary.validation.strict-mode:false}")
    private boolean strictMode;
    
    private final ThirdPartyValidationClient validationClient;
    
    public BeneficiaryValidationService(ThirdPartyValidationClient validationClient) {
        this.validationClient = validationClient;
    }
    
    /**
     * Validates a beneficiary request against third-party services.
     * 
     * @param request The beneficiary request to validate
     * @throws BeneficiaryValidationException if validation fails
     */
    public void validateBeneficiary(BeneficiaryRequest request) {
        if (!validationEnabled) {
            log.debug("Beneficiary validation is disabled");
            return;
        }
        
        log.info("Validating beneficiary: {} for account: {}", 
                request.getBeneficiaryName(), 
                request.getBeneficiaryAccountNumber());
        
        // Step 1: Basic format validation
        validateAccountFormat(request);
        
        // Step 2: Third-party validation
        performThirdPartyValidation(request);
        
        log.info("Beneficiary validation successful for account: {}", 
                request.getBeneficiaryAccountNumber());
    }
    
    /**
     * Validates basic account format and bank code.
     */
    private void validateAccountFormat(BeneficiaryRequest request) {
        // Validate account number format
        if (request.getBeneficiaryAccountNumber() == null || 
            !ACCOUNT_NUMBER_PATTERN.matcher(request.getBeneficiaryAccountNumber()).matches()) {
            throw new BeneficiaryValidationException(
                    "Invalid account number format. Must be 8-20 digits.");
        }
        
        // Validate bank code format
        if (request.getBeneficiaryBankCode() == null || 
            !BANK_CODE_PATTERN.matcher(request.getBeneficiaryBankCode()).matches()) {
            throw new BeneficiaryValidationException(
                    "Invalid bank code format. Must be 6-11 alphanumeric characters.");
        }
        
        // Validate beneficiary name
        if (request.getBeneficiaryName() == null || request.getBeneficiaryName().trim().isEmpty()) {
            throw new BeneficiaryValidationException("Beneficiary name is required.");
        }
        
        if (request.getBeneficiaryName().length() < 2) {
            throw new BeneficiaryValidationException(
                    "Beneficiary name must be at least 2 characters.");
        }
        
        if (request.getBeneficiaryName().length() > 100) {
            throw new BeneficiaryValidationException(
                    "Beneficiary name must not exceed 100 characters.");
        }
    }
    
    /**
     * Performs validation against third-party services including:
     * - Account existence verification
     * - Fraud screening
     * - Sanctions list checking
     */
    private void performThirdPartyValidation(BeneficiaryRequest request) {
        try {
            // Call third-party validation service
            ThirdPartyValidationResponse response = validationClient.validateBeneficiary(
                    request.getBeneficiaryAccountNumber(),
                    request.getBeneficiaryBankCode(),
                    request.getBeneficiaryName(),
                    request.getBeneficiaryType()
            );
            
            // Check validation result
            if (!response.isValid()) {
                String reason = response.getFailureReason() != null 
                        ? response.getFailureReason() 
                        : "Account validation failed";
                throw new BeneficiaryValidationException(
                        "Third-party validation failed: " + reason);
            }
            
            // Check fraud score
            if (response.getFraudScore() != null && response.getFraudScore() > 0.7) {
                if (strictMode) {
                    throw new BeneficiaryValidationException(
                            "High fraud risk detected. Beneficiary cannot be added.");
                } else {
                    log.warn("High fraud score detected for beneficiary: {} (score: {})", 
                            request.getBeneficiaryAccountNumber(), 
                            response.getFraudScore());
                }
            }
            
            // Check sanctions screening
            if (response.isSanctioned()) {
                throw new BeneficiaryValidationException(
                        "Beneficiary is on sanctions list and cannot be added.");
            }
            
            // Check account status
            if (response.getAccountStatus() != null && 
                !response.getAccountStatus().equalsIgnoreCase("ACTIVE")) {
                throw new BeneficiaryValidationException(
                        "Beneficiary account is not active: " + response.getAccountStatus());
            }
            
        } catch (BeneficiaryValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during third-party validation", e);
            if (strictMode) {
                throw new BeneficiaryValidationException(
                        "Unable to validate beneficiary with third-party service: " + e.getMessage());
            } else {
                log.warn("Third-party validation failed but continuing in non-strict mode");
            }
        }
    }
}
