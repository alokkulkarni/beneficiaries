package com.alok.payment.beneficiaries.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BeneficiaryRequest {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    private String accountNumber;
    
    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;
    
    @NotBlank(message = "Beneficiary account number is required")
    private String beneficiaryAccountNumber;
    
    @NotBlank(message = "Beneficiary bank code is required")
    private String beneficiaryBankCode;
    
    private String beneficiaryBankName;
    
    @Pattern(regexp = "DOMESTIC|INTERNATIONAL", message = "Beneficiary type must be DOMESTIC or INTERNATIONAL")
    private String beneficiaryType;

    public BeneficiaryRequest() {
    }

    public BeneficiaryRequest(String customerId, String accountNumber, String beneficiaryName,
                              String beneficiaryAccountNumber, String beneficiaryBankCode,
                              String beneficiaryBankName, String beneficiaryType) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.beneficiaryBankCode = beneficiaryBankCode;
        this.beneficiaryBankName = beneficiaryBankName;
        this.beneficiaryType = beneficiaryType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getBeneficiaryBankCode() {
        return beneficiaryBankCode;
    }

    public void setBeneficiaryBankCode(String beneficiaryBankCode) {
        this.beneficiaryBankCode = beneficiaryBankCode;
    }

    public String getBeneficiaryBankName() {
        return beneficiaryBankName;
    }

    public void setBeneficiaryBankName(String beneficiaryBankName) {
        this.beneficiaryBankName = beneficiaryBankName;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }
}
