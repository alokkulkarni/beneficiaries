package com.alok.payment.beneficiaries.service;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.exception.BeneficiaryNotFoundException;
import com.alok.payment.beneficiaries.exception.DuplicateBeneficiaryException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BeneficiaryService {
    
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryService.class);
    
    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryValidationService validationService;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository,
                             BeneficiaryValidationService validationService) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.validationService = validationService;
    }
    
    @Transactional
    public Beneficiary createBeneficiary(BeneficiaryRequest request) {
        log.info("Creating beneficiary for customer: {}", request.getCustomerId());
        
        // Validate beneficiary with third-party service
        validationService.validateBeneficiary(request);
        
        // Check for duplicate beneficiary account number
        beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(
                request.getCustomerId(), 
                request.getBeneficiaryAccountNumber()
        ).ifPresent(existing -> {
            throw new DuplicateBeneficiaryException(
                    "Beneficiary with account number " + request.getBeneficiaryAccountNumber() + 
                    " already exists for customer " + request.getCustomerId()
            );
        });
        
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerId(request.getCustomerId());
        beneficiary.setAccountNumber(request.getAccountNumber());
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());
        beneficiary.setBeneficiaryBankCode(request.getBeneficiaryBankCode());
        beneficiary.setBeneficiaryBankName(request.getBeneficiaryBankName());
        beneficiary.setBeneficiaryType(request.getBeneficiaryType() != null ? request.getBeneficiaryType() : "DOMESTIC");
        beneficiary.setStatus("ACTIVE");
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        
        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        log.info("Beneficiary created with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional
    public Beneficiary updateBeneficiary(Long id, String customerId, BeneficiaryRequest request) {
        log.info("Updating beneficiary ID: {} for customer: {}", id, customerId);
        
        Beneficiary existing = beneficiaryRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new BeneficiaryNotFoundException(
                        "Beneficiary not found with ID: " + id + " for customer: " + customerId
                ));
        
        // Check if updating to a different beneficiary account number that already exists
        if (!existing.getBeneficiaryAccountNumber().equals(request.getBeneficiaryAccountNumber())) {
            beneficiaryRepository.findByCustomerIdAndBeneficiaryAccountNumber(
                    customerId, 
                    request.getBeneficiaryAccountNumber()
            ).ifPresent(duplicate -> {
                throw new DuplicateBeneficiaryException(
                        "Beneficiary with account number " + request.getBeneficiaryAccountNumber() + 
                        " already exists for customer " + customerId
                );
            });
        }
        
        Beneficiary updated = new Beneficiary();
        updated.setId(existing.getId());
        updated.setCustomerId(customerId);
        updated.setAccountNumber(request.getAccountNumber());
        updated.setBeneficiaryName(request.getBeneficiaryName());
        updated.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());
        updated.setBeneficiaryBankCode(request.getBeneficiaryBankCode());
        updated.setBeneficiaryBankName(request.getBeneficiaryBankName());
        updated.setBeneficiaryType(request.getBeneficiaryType() != null ? request.getBeneficiaryType() : existing.getBeneficiaryType());
        updated.setStatus(existing.getStatus());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        
        Beneficiary saved = beneficiaryRepository.save(updated);
        log.info("Beneficiary updated with ID: {}", saved.getId());
        return saved;
    }
    
    @Transactional
    public void deleteBeneficiary(Long id, String customerId) {
        log.info("Deleting beneficiary ID: {} for customer: {}", id, customerId);
        
        int deleted = beneficiaryRepository.softDeleteByIdAndCustomerId(id, customerId);
        if (deleted == 0) {
            throw new BeneficiaryNotFoundException(
                    "Beneficiary not found with ID: " + id + " for customer: " + customerId
            );
        }
        
        log.info("Beneficiary soft deleted with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Beneficiary getBeneficiary(Long id, String customerId) {
        log.info("Fetching beneficiary ID: {} for customer: {}", id, customerId);
        
        return beneficiaryRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new BeneficiaryNotFoundException(
                        "Beneficiary not found with ID: " + id + " for customer: " + customerId
                ));
    }
    
    @Transactional(readOnly = true)
    public List<Beneficiary> getBeneficiaries(String customerId, String accountNumber) {
        log.info("Fetching beneficiaries for customer: {}, account: {}", customerId, accountNumber);
        
        if (accountNumber != null && !accountNumber.isBlank()) {
            return beneficiaryRepository.findByCustomerIdAndAccountNumber(customerId, accountNumber);
        } else {
            return beneficiaryRepository.findByCustomerId(customerId);
        }
    }
}
