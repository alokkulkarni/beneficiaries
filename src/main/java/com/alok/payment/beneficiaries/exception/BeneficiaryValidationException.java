package com.alok.payment.beneficiaries.exception;

/**
 * Exception thrown when beneficiary validation fails.
 */
public class BeneficiaryValidationException extends RuntimeException {
    
    public BeneficiaryValidationException(String message) {
        super(message);
    }
    
    public BeneficiaryValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
