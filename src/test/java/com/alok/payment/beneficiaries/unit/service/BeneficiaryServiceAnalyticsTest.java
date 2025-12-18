package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import com.alok.payment.beneficiaries.service.BeneficiaryValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryService Analytics Tests")
class BeneficiaryServiceAnalyticsTest {
    
    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    
    @Mock
    private BeneficiaryValidationService validationService;
    
    @InjectMocks
    private BeneficiaryService beneficiaryService;
    
    private List<Beneficiary> testBeneficiaries;
    
    @BeforeEach
    void setUp() {
        // Create test beneficiaries with different properties
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setCustomerId("CUST001");
        ben1.setBeneficiaryName("John Doe");
        ben1.setBeneficiaryAccountNumber("ACC001");
        ben1.setBeneficiaryBankName("Bank A");
        ben1.setBeneficiaryType("DOMESTIC");
        ben1.setStatus("ACTIVE");
        ben1.setCreatedAt(LocalDateTime.now().minusDays(5));
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setCustomerId("CUST001");
        ben2.setBeneficiaryName("Jane Smith");
        ben2.setBeneficiaryAccountNumber("ACC002");
        ben2.setBeneficiaryBankName("Bank B");
        ben2.setBeneficiaryType("INTERNATIONAL");
        ben2.setStatus("ACTIVE");
        ben2.setCreatedAt(LocalDateTime.now().minusDays(3));
        
        Beneficiary ben3 = new Beneficiary();
        ben3.setId(3L);
        ben3.setCustomerId("CUST001");
        ben3.setBeneficiaryName("Bob Wilson");
        ben3.setBeneficiaryAccountNumber("ACC003");
        ben3.setBeneficiaryBankName("Bank A");
        ben3.setBeneficiaryType("DOMESTIC");
        ben3.setStatus("INACTIVE");
        ben3.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        testBeneficiaries = Arrays.asList(ben1, ben2, ben3);
    }
    
    @Test
    @DisplayName("Should generate analytics with correct counts")
    void shouldGenerateAnalyticsWithCorrectCounts() {
        // Given
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        assertThat(analytics).isNotNull();
        assertThat(analytics.get("customerId")).isEqualTo("CUST001");
        assertThat(analytics.get("totalBeneficiaries")).isEqualTo(3);
        assertThat(analytics.get("activeBeneficiaries")).isEqualTo(2L);
        assertThat(analytics.get("inactiveBeneficiaries")).isEqualTo(1L);
        
        verify(beneficiaryRepository).findAllByCustomerId("CUST001");
    }
    
    @Test
    @DisplayName("Should group beneficiaries by type correctly")
    void shouldGroupBeneficiariesByType() {
        // Given
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        @SuppressWarnings("unchecked")
        Map<String, Long> byType = (Map<String, Long>) analytics.get("beneficiariesByType");
        assertThat(byType).isNotNull();
        assertThat(byType.get("DOMESTIC")).isEqualTo(2L);
        assertThat(byType.get("INTERNATIONAL")).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should group beneficiaries by bank correctly")
    void shouldGroupBeneficiariesByBank() {
        // Given
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        @SuppressWarnings("unchecked")
        Map<String, Long> byBank = (Map<String, Long>) analytics.get("beneficiariesByBank");
        assertThat(byBank).isNotNull();
        assertThat(byBank.get("Bank A")).isEqualTo(2L);
        assertThat(byBank.get("Bank B")).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should identify most recent beneficiary")
    void shouldIdentifyMostRecentBeneficiary() {
        // Given
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        assertThat(analytics.get("mostRecentBeneficiaryName")).isEqualTo("Bob Wilson");
        assertThat(analytics.get("mostRecentAddedAt")).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle empty beneficiary list for analytics")
    void shouldHandleEmptyListForAnalytics() {
        // Given
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(List.of());
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        assertThat(analytics).isNotNull();
        assertThat(analytics.get("totalBeneficiaries")).isEqualTo(0);
        assertThat(analytics.get("activeBeneficiaries")).isEqualTo(0L);
        assertThat(analytics.get("mostRecentBeneficiaryName")).isNull();
    }
    
    @Test
    @DisplayName("Should find potential duplicates based on similar names")
    void shouldFindPotentialDuplicates() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setBeneficiaryName("John Doe");
        ben1.setBeneficiaryAccountNumber("ACC001");
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setBeneficiaryName("JohnDoe");
        ben2.setBeneficiaryAccountNumber("ACC002");
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(Arrays.asList(ben1, ben2));
        
        // When
        List<Map<String, Object>> duplicates = beneficiaryService.findPotentialDuplicates("CUST001");
        
        // Then
        assertThat(duplicates).isNotEmpty();
        assertThat(duplicates.get(0).get("beneficiary1Name")).isEqualTo("John Doe");
        assertThat(duplicates.get(0).get("beneficiary2Name")).isEqualTo("JohnDoe");
        assertThat(duplicates.get(0).get("similarity")).isEqualTo("HIGH");
    }
    
    @Test
    @DisplayName("Should not find duplicates when names are different")
    void shouldNotFindDuplicatesWhenNamesDifferent() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setBeneficiaryName("John Doe");
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setBeneficiaryName("Jane Smith");
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(Arrays.asList(ben1, ben2));
        
        // When
        List<Map<String, Object>> duplicates = beneficiaryService.findPotentialDuplicates("CUST001");
        
        // Then
        assertThat(duplicates).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle null names in duplicate detection")
    void shouldHandleNullNamesInDuplicateDetection() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setBeneficiaryName(null);
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setBeneficiaryName("Jane Smith");
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(Arrays.asList(ben1, ben2));
        
        // When
        List<Map<String, Object>> duplicates = beneficiaryService.findPotentialDuplicates("CUST001");
        
        // Then
        assertThat(duplicates).isEmpty();
    }
    
    @Test
    @DisplayName("Should generate usage report for a time period")
    void shouldGenerateUsageReport() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(4);
        LocalDateTime endDate = now;
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> report = beneficiaryService.getBeneficiaryUsageReport(
                "CUST001", startDate, endDate);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.get("customerId")).isEqualTo("CUST001");
        assertThat(report.get("totalBeneficiaries")).isEqualTo(3);
        assertThat(report.get("beneficiariesAddedInPeriod")).isEqualTo(2);
        assertThat(report.get("growthRatePercent")).isNotNull();
    }
    
    @Test
    @DisplayName("Should calculate growth rate correctly")
    void shouldCalculateGrowthRateCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(4);
        LocalDateTime endDate = now;
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> report = beneficiaryService.getBeneficiaryUsageReport(
                "CUST001", startDate, endDate);
        
        // Then
        Double growthRate = (Double) report.get("growthRatePercent");
        assertThat(growthRate).isNotNull();
        // 2 out of 3 = 66.67%
        assertThat(growthRate).isBetween(66.0, 67.0);
    }
    
    @Test
    @DisplayName("Should identify most active day in usage report")
    void shouldIdentifyMostActiveDay() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(10);
        LocalDateTime endDate = now;
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(testBeneficiaries);
        
        // When
        Map<String, Object> report = beneficiaryService.getBeneficiaryUsageReport(
                "CUST001", startDate, endDate);
        
        // Then
        assertThat(report.get("mostActiveDay")).isNotNull();
        assertThat(report.get("beneficiariesAddedOnMostActiveDay")).isNotNull();
    }
    
    @Test
    @DisplayName("Should handle empty list in usage report")
    void shouldHandleEmptyListInUsageReport() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(10);
        LocalDateTime endDate = now;
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(List.of());
        
        // When
        Map<String, Object> report = beneficiaryService.getBeneficiaryUsageReport(
                "CUST001", startDate, endDate);
        
        // Then
        assertThat(report).isNotNull();
        assertThat(report.get("totalBeneficiaries")).isEqualTo(0);
        assertThat(report.get("beneficiariesAddedInPeriod")).isEqualTo(0);
        assertThat(report.get("growthRatePercent")).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should detect similar names with Levenshtein distance")
    void shouldDetectSimilarNamesWithLevenshtein() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setBeneficiaryName("John Doe");
        ben1.setBeneficiaryAccountNumber("ACC001");
        
        Beneficiary ben2 = new Beneficiary();
        ben2.setId(2L);
        ben2.setBeneficiaryName("Jon Doe");  // 1 character difference
        ben2.setBeneficiaryAccountNumber("ACC002");
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(Arrays.asList(ben1, ben2));
        
        // When
        List<Map<String, Object>> duplicates = beneficiaryService.findPotentialDuplicates("CUST001");
        
        // Then
        assertThat(duplicates).isNotEmpty();
    }
    
    @Test
    @DisplayName("Should handle beneficiaries with null type in analytics")
    void shouldHandleBeneficiariesWithNullType() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setCustomerId("CUST001");
        ben1.setBeneficiaryName("Test User");
        ben1.setBeneficiaryType(null);  // Null type
        ben1.setStatus("ACTIVE");
        ben1.setCreatedAt(LocalDateTime.now());
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(List.of(ben1));
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        @SuppressWarnings("unchecked")
        Map<String, Long> byType = (Map<String, Long>) analytics.get("beneficiariesByType");
        assertThat(byType).isNotNull();
        assertThat(byType.get("DOMESTIC")).isEqualTo(1L);  // Defaults to DOMESTIC
    }
    
    @Test
    @DisplayName("Should handle beneficiaries with null bank name in analytics")
    void shouldHandleBeneficiariesWithNullBankName() {
        // Given
        Beneficiary ben1 = new Beneficiary();
        ben1.setId(1L);
        ben1.setCustomerId("CUST001");
        ben1.setBeneficiaryName("Test User");
        ben1.setBeneficiaryBankName(null);  // Null bank name
        ben1.setStatus("ACTIVE");
        ben1.setCreatedAt(LocalDateTime.now());
        
        when(beneficiaryRepository.findAllByCustomerId("CUST001"))
                .thenReturn(List.of(ben1));
        
        // When
        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST001");
        
        // Then
        @SuppressWarnings("unchecked")
        Map<String, Long> byBank = (Map<String, Long>) analytics.get("beneficiariesByBank");
        assertThat(byBank).isNotNull().isEmpty();  // Should not include null banks
    }
}
