package com.alok.payment.beneficiaries.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for auditing beneficiary operations.
 * Logs all create, update, and delete operations for compliance and tracking.
 */
@Service
public class BeneficiaryAuditService {
    
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryAuditService.class);
    
    /**
     * Logs beneficiary creation event.
     */
    public void logBeneficiaryCreated(String customerId, Long beneficiaryId, String accountNumber) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("operation", "CREATE");
        auditData.put("customerId", customerId);
        auditData.put("beneficiaryId", beneficiaryId);
        auditData.put("accountNumber", accountNumber);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.info("Beneficiary audit: {}", auditData);
    }
    
    /**
     * Logs beneficiary update event.
     */
    public void logBeneficiaryUpdated(String customerId, Long beneficiaryId, Map<String, String> changes) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("operation", "UPDATE");
        auditData.put("customerId", customerId);
        auditData.put("beneficiaryId", beneficiaryId);
        auditData.put("changes", changes);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.info("Beneficiary audit: {}", auditData);
    }
    
    /**
     * Logs beneficiary deletion event.
     */
    public void logBeneficiaryDeleted(String customerId, Long beneficiaryId) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("operation", "DELETE");
        auditData.put("customerId", customerId);
        auditData.put("beneficiaryId", beneficiaryId);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.info("Beneficiary audit: {}", auditData);
    }
}
