package com.alok.payment.beneficiaries.exception;

public class DuplicateBeneficiaryException extends RuntimeException {
    public DuplicateBeneficiaryException(String message) {
        super(message);
    }
}
