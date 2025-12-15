package com.alok.payment.beneficiaries.service;

/**
 * Response from third-party beneficiary validation service.
 */
public class ThirdPartyValidationResponse {
    
    private boolean valid;
    private boolean sanctioned;
    private Double fraudScore;
    private String accountStatus;
    private String failureReason;
    
    public ThirdPartyValidationResponse() {
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public boolean isSanctioned() {
        return sanctioned;
    }
    
    public void setSanctioned(boolean sanctioned) {
        this.sanctioned = sanctioned;
    }
    
    public Double getFraudScore() {
        return fraudScore;
    }
    
    public void setFraudScore(Double fraudScore) {
        this.fraudScore = fraudScore;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
