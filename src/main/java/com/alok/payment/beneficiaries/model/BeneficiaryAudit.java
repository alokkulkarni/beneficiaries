package com.alok.payment.beneficiaries.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("beneficiary_audits")
public class BeneficiaryAudit {
    
    @Id
    private Long id;
    private Long beneficiaryId;
    private String customerId;
    private String operation; // CREATE, UPDATE, DELETE
    private String changes;
    private String performedBy;
    private LocalDateTime performedAt;

    public BeneficiaryAudit() {
    }

    public BeneficiaryAudit(Long id, Long beneficiaryId, String customerId, String operation, 
                           String changes, String performedBy, LocalDateTime performedAt) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.customerId = customerId;
        this.operation = operation;
        this.changes = changes;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(Long beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}
