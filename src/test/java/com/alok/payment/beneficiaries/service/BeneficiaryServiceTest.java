package com.alok.payment.beneficiaries.service;

import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Simple unit test to ensure analytics uses findAllByCustomerId and processes all statuses.
 */
@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private BeneficiaryValidationService validationService;

    private BeneficiaryService beneficiaryService;

    @BeforeEach
    void setUp() {
        beneficiaryService = new BeneficiaryService(beneficiaryRepository, validationService);
    }

    @Test
    @DisplayName("Analytics should use findAllByCustomerId and count active/inactive correctly")
    void analyticsCountsAllStatuses() {
        Beneficiary active = new Beneficiary();
        active.setCustomerId("CUST_TEST");
        active.setStatus("ACTIVE");
        active.setBeneficiaryName("Active User");
        active.setBeneficiaryType("DOMESTIC");
        active.setBeneficiaryBankName("Bank A");
        active.setCreatedAt(LocalDateTime.now().minusDays(2));

        Beneficiary inactive = new Beneficiary();
        inactive.setCustomerId("CUST_TEST");
        inactive.setStatus("INACTIVE");
        inactive.setBeneficiaryName("Inactive User");
        inactive.setBeneficiaryType("INTERNATIONAL");
        inactive.setBeneficiaryBankName("Bank B");
        inactive.setCreatedAt(LocalDateTime.now().minusDays(1));

        Beneficiary deleted = new Beneficiary();
        deleted.setCustomerId("CUST_TEST");
        deleted.setStatus("DELETED");
        deleted.setBeneficiaryName("Deleted User");
        deleted.setBeneficiaryType("DOMESTIC");
        deleted.setBeneficiaryBankName("Bank A");
        deleted.setCreatedAt(LocalDateTime.now());

        when(beneficiaryRepository.findAllByCustomerId("CUST_TEST"))
                .thenReturn(Arrays.asList(active, inactive, deleted));

        Map<String, Object> analytics = beneficiaryService.getCustomerBeneficiaryAnalytics("CUST_TEST");

        verify(beneficiaryRepository).findAllByCustomerId("CUST_TEST");

        Assertions.assertThat(((Number) analytics.get("totalBeneficiaries")).intValue()).isEqualTo(3);
        Assertions.assertThat(((Number) analytics.get("activeBeneficiaries")).intValue()).isEqualTo(1);
        Assertions.assertThat(((Number) analytics.get("inactiveBeneficiaries")).intValue()).isEqualTo(1);

        Map<String, Long> byType = (Map<String, Long>) analytics.get("beneficiariesByType");
        Assertions.assertThat(byType.get("DOMESTIC")).isEqualTo(2L);
        Assertions.assertThat(byType.get("INTERNATIONAL")).isEqualTo(1L);

        Map<String, Long> byBank = (Map<String, Long>) analytics.get("beneficiariesByBank");
        Assertions.assertThat(byBank.get("Bank A")).isEqualTo(2L);
        Assertions.assertThat(byBank.get("Bank B")).isEqualTo(1L);

        Assertions.assertThat(analytics.get("mostRecentBeneficiaryName")).isEqualTo("Deleted User");
        Assertions.assertThat(analytics.get("mostRecentAddedAt")).isEqualTo(deleted.getCreatedAt());
    }
}
