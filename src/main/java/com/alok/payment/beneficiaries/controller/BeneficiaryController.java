package com.alok.payment.beneficiaries.controller;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.dto.BeneficiarySearchCriteria;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    
    private static final Logger log = LoggerFactory.getLogger(BeneficiaryController.class);
    
    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }
    
    @PostMapping
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request) {
        log.info("REST request to create beneficiary for customer: {}", request.getCustomerId());
        
        Beneficiary beneficiary = beneficiaryService.createBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BeneficiaryResponse.from(beneficiary));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable Long id,
            @RequestParam String customerId,
            @Valid @RequestBody BeneficiaryRequest request) {
        log.info("REST request to update beneficiary ID: {} for customer: {}", id, customerId);
        
        Beneficiary beneficiary = beneficiaryService.updateBeneficiary(id, customerId, request);
        return ResponseEntity.ok(BeneficiaryResponse.from(beneficiary));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @PathVariable Long id,
            @RequestParam String customerId) {
        log.info("REST request to delete beneficiary ID: {} for customer: {}", id, customerId);
        
        beneficiaryService.deleteBeneficiary(id, customerId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiary(
            @PathVariable Long id,
            @RequestParam String customerId) {
        log.info("REST request to get beneficiary ID: {} for customer: {}", id, customerId);
        
        Beneficiary beneficiary = beneficiaryService.getBeneficiary(id, customerId);
        return ResponseEntity.ok(BeneficiaryResponse.from(beneficiary));
    }
    
    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getBeneficiaries(
            @RequestParam String customerId,
            @RequestParam(required = false) String accountNumber) {
        log.info("REST request to get beneficiaries for customer: {}, account: {}", 
                customerId, accountNumber);
        
        List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiaries(customerId, accountNumber);
        List<BeneficiaryResponse> responses = beneficiaries.stream()
                .map(BeneficiaryResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<BeneficiaryResponse>> searchBeneficiaries(
            @RequestBody BeneficiarySearchCriteria criteria) {
        log.info("REST request to search beneficiaries with criteria");
        
        PagedResponse<Beneficiary> pagedResult = beneficiaryService.searchBeneficiaries(criteria);
        
        List<BeneficiaryResponse> responses = pagedResult.getContent().stream()
                .map(BeneficiaryResponse::from)
                .collect(Collectors.toList());
        
        PagedResponse<BeneficiaryResponse> response = new PagedResponse<>(
                responses,
                pagedResult.getPage(),
                pagedResult.getSize(),
                pagedResult.getTotalElements()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getBeneficiaryAnalytics(
            @RequestParam String customerId) {
        log.info("REST request to get beneficiary analytics for customer: {}", customerId);
        
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics(customerId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/duplicates")
    public ResponseEntity<List<Map<String, Object>>> findPotentialDuplicates(
            @RequestParam String customerId) {
        log.info("REST request to find potential duplicate beneficiaries for customer: {}", customerId);
        
        List<Map<String, Object>> duplicates = beneficiaryService.findPotentialDuplicates(customerId);
        return ResponseEntity.ok(duplicates);
    }
    
    @GetMapping("/usage-report")
    public ResponseEntity<Map<String, Object>> getUsageReport(
            @RequestParam String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("REST request to get usage report for customer: {} from {} to {}", 
                customerId, startDate, endDate);
        
        Map<String, Object> report = beneficiaryService.getBeneficiaryUsageReport(
                customerId, startDate, endDate);
        return ResponseEntity.ok(report);
    }}