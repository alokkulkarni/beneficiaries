package com.alok.payment.beneficiaries.unit.service;

import com.alok.payment.beneficiaries.dto.BeneficiarySearchCriteria;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.repository.BeneficiaryRepository;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import com.alok.payment.beneficiaries.service.BeneficiaryValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BeneficiaryService Search Unit Tests")
class BeneficiaryServiceSearchTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private BeneficiaryValidationService validationService;

    @InjectMocks
    private BeneficiaryService beneficiaryService;

    @Test
    @DisplayName("searchBeneficiaries returns paged response with mapping and counts")
    void searchBeneficiariesReturnsPagedResponse() {
        BeneficiarySearchCriteria criteria = new BeneficiarySearchCriteria();
        criteria.setCustomerId("CUST_SVC");
        criteria.setBeneficiaryName("john");
        criteria.setBeneficiaryType("DOMESTIC");
        criteria.setStatus("ACTIVE");
        criteria.setBeneficiaryBankCode("BANKX");
        criteria.setCreatedAfter(LocalDateTime.now().minusDays(10));
        criteria.setCreatedBefore(LocalDateTime.now());
        criteria.setPage(1);
        criteria.setSize(5);
        criteria.setSortBy("createdAt");
        criteria.setSortDirection("DESC");

        Beneficiary b = new Beneficiary();
        b.setId(101L);
        b.setCustomerId("CUST_SVC");
        b.setBeneficiaryName("John Alpha");
        b.setBeneficiaryType("DOMESTIC");
        b.setBeneficiaryBankCode("BANKX");
        b.setStatus("ACTIVE");
        b.setCreatedAt(LocalDateTime.now().minusDays(1));
        b.setUpdatedAt(LocalDateTime.now());

        when(beneficiaryRepository.searchBeneficiaries(
                anyString(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()
        )).thenReturn(List.of(b));

        when(beneficiaryRepository.countBeneficiaries(
                anyString(), any(), any(), any(), any(), any(), any()
        )).thenReturn(17L);

        PagedResponse<Beneficiary> response = beneficiaryService.searchBeneficiaries(criteria);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNotNull().hasSize(1);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(5);
        assertThat(response.getTotalElements()).isEqualTo(17L);
        assertThat(response.getTotalPages()).isEqualTo((int) Math.ceil(17.0 / 5));
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isEqualTo(1 >= response.getTotalPages() - 1);

        verify(beneficiaryRepository).searchBeneficiaries(
                eq("CUST_SVC"), eq("john"), eq("DOMESTIC"), eq("ACTIVE"), eq("BANKX"),
                any(LocalDateTime.class), any(LocalDateTime.class), eq("createdAt"), eq("DESC"), eq(5), eq(5)
        );

        verify(beneficiaryRepository).countBeneficiaries(
                eq("CUST_SVC"), eq("john"), eq("DOMESTIC"), eq("ACTIVE"), eq("BANKX"),
                any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }
}
