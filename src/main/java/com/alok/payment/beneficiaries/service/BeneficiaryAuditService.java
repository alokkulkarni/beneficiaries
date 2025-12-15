package com.alok.payment.beneficiaries.service;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.model.BeneficiaryAudit;
import com.alok.payment.beneficiaries.repository.BeneficiaryAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BeneficiaryAuditService {
    
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryAuditService.class);
    
    private final BeneficiaryAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public BeneficiaryAuditService(BeneficiaryAuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }
    
    @Transactional
    public BeneficiaryAudit auditCreate(Beneficiary beneficiary, String performedBy) {
        log.info("Auditing beneficiary creation for ID: {}", beneficiary.getId());
        
        BeneficiaryAudit audit = new BeneficiaryAudit();
        audit.setBeneficiaryId(beneficiary.getId());
        audit.setCustomerId(beneficiary.getCustomerId());
        audit.setOperation("CREATE");
        audit.setChanges(serializeBeneficiary(beneficiary));
        audit.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        audit.setPerformedAt(LocalDateTime.now());
        
        BeneficiaryAudit saved = auditRepository.save(audit);
        log.info("Audit record created with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional
    public BeneficiaryAudit auditUpdate(Beneficiary beneficiary, String performedBy) {
        log.info("Auditing beneficiary update for ID: {}", beneficiary.getId());
        
        BeneficiaryAudit audit = new BeneficiaryAudit();
        audit.setBeneficiaryId(beneficiary.getId());
        audit.setCustomerId(beneficiary.getCustomerId());
        audit.setOperation("UPDATE");
        audit.setChanges(serializeBeneficiary(beneficiary));
        audit.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        audit.setPerformedAt(LocalDateTime.now());
        
        BeneficiaryAudit saved = auditRepository.save(audit);
        log.info("Audit record created with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional
    public BeneficiaryAudit auditDelete(Long beneficiaryId, String customerId, String performedBy) {
        log.info("Auditing beneficiary deletion for ID: {}", beneficiaryId);
        
        BeneficiaryAudit audit = new BeneficiaryAudit();
        audit.setBeneficiaryId(beneficiaryId);
        audit.setCustomerId(customerId);
        audit.setOperation("DELETE");
        audit.setChanges(serializeDeleteInfo(beneficiaryId, customerId));
        audit.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        audit.setPerformedAt(LocalDateTime.now());
        
        BeneficiaryAudit saved = auditRepository.save(audit);
        log.info("Audit record created with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional(readOnly = true)
    public List<BeneficiaryAudit> getAuditHistory(Long beneficiaryId, String customerId) {
        log.info("Fetching audit history for beneficiary ID: {} and customer: {}", beneficiaryId, customerId);
        return auditRepository.findByBeneficiaryIdAndCustomerId(beneficiaryId, customerId);
    }
    
    @Transactional(readOnly = true)
    public List<BeneficiaryAudit> getCustomerAuditHistory(String customerId) {
        log.info("Fetching audit history for customer: {}", customerId);
        return auditRepository.findByCustomerIdOrderByPerformedAtDesc(customerId);
    }
    
    private String serializeBeneficiary(Beneficiary beneficiary) {
        try {
            return objectMapper.writeValueAsString(beneficiary);
        } catch (JsonProcessingException e) {
            log.error("Error serializing beneficiary", e);
            return "{}";
        }
    }
    
    private String serializeDeleteInfo(Long beneficiaryId, String customerId) {
        try {
            java.util.Map<String, Object> deleteInfo = new java.util.HashMap<>();
            deleteInfo.put("beneficiaryId", beneficiaryId);
            deleteInfo.put("customerId", customerId);
            return objectMapper.writeValueAsString(deleteInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing delete info", e);
            return "{}";
        }
    }
}
