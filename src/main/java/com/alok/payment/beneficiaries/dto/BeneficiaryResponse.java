package com.alok.payment.beneficiaries.dto;

import com.alok.payment.beneficiaries.model.Beneficiary;

import java.time.LocalDateTime;

public class BeneficiaryResponse {
    
    private Long id;
    private String customerId;
    private String accountNumber;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String beneficiaryBankCode;
    private String beneficiaryBankName;
    private String beneficiaryType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BeneficiaryResponse() {
    }

    public BeneficiaryResponse(Long id, String customerId, String accountNumber, String beneficiaryName,
                               String beneficiaryAccountNumber, String beneficiaryBankCode, String beneficiaryBankName,
                               String beneficiaryType, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.beneficiaryBankCode = beneficiaryBankCode;
        this.beneficiaryBankName = beneficiaryBankName;
        this.beneficiaryType = beneficiaryType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public static BeneficiaryResponse from(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getCustomerId(),
                beneficiary.getAccountNumber(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBeneficiaryAccountNumber(),
                beneficiary.getBeneficiaryBankCode(),
                beneficiary.getBeneficiaryBankName(),
                beneficiary.getBeneficiaryType(),
                beneficiary.getStatus(),
                beneficiary.getCreatedAt(),
                beneficiary.getUpdatedAt()
        );
    }
}
