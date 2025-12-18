package com.alok.payment.beneficiaries.service;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiarySearchCriteria;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import com.alok.payment.beneficiaries.exception.BeneficiaryNotFoundException;
import com.alok.payment.beneficiaries.exception.DuplicateBeneficiaryException;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    /**
     * NEW: Search beneficiaries with advanced filtering and pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<Beneficiary> searchBeneficiaries(BeneficiarySearchCriteria criteria) {
        log.info("Searching beneficiaries with criteria - customer: {}, name: {}, type: {}, status: {}", 
                criteria.getCustomerId(), criteria.getBeneficiaryName(), 
                criteria.getBeneficiaryType(), criteria.getStatus());
        
        int limit = criteria.getSize();
        int offset = criteria.getPage() * criteria.getSize();
        
        List<Beneficiary> results = beneficiaryRepository.searchBeneficiaries(
                criteria.getCustomerId(),
                criteria.getBeneficiaryName(),
                criteria.getBeneficiaryType(),
                criteria.getStatus(),
                criteria.getBeneficiaryBankCode(),
                criteria.getCreatedAfter(),
                criteria.getCreatedBefore(),
                criteria.getSortBy(),
                criteria.getSortDirection(),
                limit,
                offset
        );
        
        long totalCount = beneficiaryRepository.countBeneficiaries(
                criteria.getCustomerId(),
                criteria.getBeneficiaryName(),
                criteria.getBeneficiaryType(),
                criteria.getStatus(),
                criteria.getBeneficiaryBankCode(),
                criteria.getCreatedAfter(),
                criteria.getCreatedBefore()
        );
        
        log.info("Search returned {} results out of {} total", results.size(), totalCount);
        
        return new PagedResponse<>(results, criteria.getPage(), criteria.getSize(), totalCount);
    }
    
    /**
     * NEW: Get comprehensive analytics for a customer's beneficiaries
     * Provides detailed metrics for compliance and monitoring
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCustomerBeneficiaryAnalytics(String customerId) {
        log.info("Generating beneficiary analytics for customer: {}", customerId);
        
        List<Beneficiary> beneficiaries = beneficiaryRepository.findAllByCustomerId(customerId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("customerId", customerId);
        analytics.put("totalBeneficiaries", beneficiaries.size());
        analytics.put("activeBeneficiaries", beneficiaries.stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .count());
        analytics.put("inactiveBeneficiaries", beneficiaries.stream()
                .filter(b -> "INACTIVE".equals(b.getStatus()))
                .count());
        
        // Group by beneficiary type
        Map<String, Long> byType = beneficiaries.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBeneficiaryType() != null ? b.getBeneficiaryType() : "DOMESTIC",
                        Collectors.counting()
                ));
        analytics.put("beneficiariesByType", byType);
        
        // Group by bank
        Map<String, Long> byBank = beneficiaries.stream()
                .filter(b -> b.getBeneficiaryBankName() != null)
                .collect(Collectors.groupingBy(
                        Beneficiary::getBeneficiaryBankName,
                        Collectors.counting()
                ));
        analytics.put("beneficiariesByBank", byBank);
        
        // Most recent beneficiary
        beneficiaries.stream()
                .max((b1, b2) -> b1.getCreatedAt().compareTo(b2.getCreatedAt()))
                .ifPresent(recent -> {
                    analytics.put("mostRecentBeneficiaryName", recent.getBeneficiaryName());
                    analytics.put("mostRecentAddedAt", recent.getCreatedAt());
                });
        
        log.info("Analytics generated for customer {}: {} total beneficiaries", 
                customerId, beneficiaries.size());
        
        return analytics;
    }
    
    /**
     * NEW: Get list of potentially duplicate beneficiaries
     * Helps identify beneficiaries that might be duplicates based on name similarity
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> findPotentialDuplicates(String customerId) {
        log.info("Searching for potential duplicate beneficiaries for customer: {}", customerId);
        
        List<Beneficiary> beneficiaries = beneficiaryRepository.findAllByCustomerId(customerId);
        
        List<Map<String, Object>> duplicates = beneficiaries.stream()
                .flatMap(b1 -> beneficiaries.stream()
                        .filter(b2 -> !b1.getId().equals(b2.getId()))
                        .filter(b2 -> areSimilarNames(b1.getBeneficiaryName(), b2.getBeneficiaryName()))
                        .map(b2 -> {
                            Map<String, Object> duplicate = new HashMap<>();
                            duplicate.put("beneficiary1Id", b1.getId());
                            duplicate.put("beneficiary1Name", b1.getBeneficiaryName());
                            duplicate.put("beneficiary1Account", b1.getBeneficiaryAccountNumber());
                            duplicate.put("beneficiary2Id", b2.getId());
                            duplicate.put("beneficiary2Name", b2.getBeneficiaryName());
                            duplicate.put("beneficiary2Account", b2.getBeneficiaryAccountNumber());
                            duplicate.put("similarity", "HIGH");
                            return duplicate;
                        })
                )
                .distinct()
                .collect(Collectors.toList());
        
        log.info("Found {} potential duplicate pairs for customer: {}", duplicates.size(), customerId);
        
        return duplicates;
    }
    
    /**
     * NEW: Get beneficiary usage report for a time period
     * Shows which beneficiaries were created within a specific time range
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBeneficiaryUsageReport(String customerId, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        log.info("Generating beneficiary usage report for customer: {} from {} to {}", 
                customerId, startDate, endDate);
        
        List<Beneficiary> allBeneficiaries = beneficiaryRepository.findAllByCustomerId(customerId);
        
        List<Beneficiary> periodBeneficiaries = allBeneficiaries.stream()
                .filter(b -> b.getCreatedAt().isAfter(startDate) && b.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("customerId", customerId);
        report.put("reportPeriodStart", startDate);
        report.put("reportPeriodEnd", endDate);
        report.put("totalBeneficiaries", allBeneficiaries.size());
        report.put("beneficiariesAddedInPeriod", periodBeneficiaries.size());
        
        // Calculate growth rate
        double growthRate = allBeneficiaries.isEmpty() ? 0.0 : 
                (double) periodBeneficiaries.size() / allBeneficiaries.size() * 100;
        report.put("growthRatePercent", Math.round(growthRate * 100.0) / 100.0);
        
        // Most active day in period
        Map<String, Long> beneficiariesByDay = periodBeneficiaries.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
        
        beneficiariesByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> {
                    report.put("mostActiveDay", entry.getKey());
                    report.put("beneficiariesAddedOnMostActiveDay", entry.getValue());
                });
        
        log.info("Usage report generated: {} beneficiaries added in period out of {} total", 
                periodBeneficiaries.size(), allBeneficiaries.size());
        
        return report;
    }
    
    /**
     * Helper method to check name similarity
     */
    private boolean areSimilarNames(String name1, String name2) {
        if (name1 == null || name2 == null) {
            return false;
        }
        
        String normalized1 = name1.toLowerCase().replaceAll("\\s+", "");
        String normalized2 = name2.toLowerCase().replaceAll("\\s+", "");
        
        // Check if names are very similar (allowing for minor variations)
        return normalized1.equals(normalized2) || 
               normalized1.contains(normalized2) || 
               normalized2.contains(normalized1) ||
               calculateLevenshteinDistance(normalized1, normalized2) <= 2;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}
