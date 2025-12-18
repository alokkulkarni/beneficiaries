package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.dto.BeneficiarySearchCriteria;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class SearchBeneficiarySteps {

    @Autowired
    private CucumberSpringConfiguration springConfiguration;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @When("I search beneficiaries with criteria:")
    public void iSearchBeneficiariesWithCriteria(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> map = dataTable.asMap(String.class, String.class);
        BeneficiarySearchCriteria criteria = new BeneficiarySearchCriteria();
        criteria.setCustomerId(map.get("customerId"));
        criteria.setBeneficiaryName(map.getOrDefault("beneficiaryName", null));
        criteria.setBeneficiaryType(map.getOrDefault("beneficiaryType", null));
        criteria.setStatus(map.getOrDefault("status", null));
        criteria.setBeneficiaryBankCode(map.getOrDefault("beneficiaryBankCode", null));

        if (map.containsKey("createdAfter")) {
            criteria.setCreatedAfter(LocalDateTime.parse(map.get("createdAfter")));
        }
        if (map.containsKey("createdBefore")) {
            criteria.setCreatedBefore(LocalDateTime.parse(map.get("createdBefore")));
        }
        if (map.containsKey("page")) {
            criteria.setPage(Integer.parseInt(map.get("page")));
        }
        if (map.containsKey("size")) {
            criteria.setSize(Integer.parseInt(map.get("size")));
        }
        criteria.setSortBy(map.getOrDefault("sortBy", criteria.getSortBy()));
        criteria.setSortDirection(map.getOrDefault("sortDirection", criteria.getSortDirection()));

        String url = "http://localhost:" + springConfiguration.getPort() + "/api/v1/beneficiaries/search";
        try {
            ResponseEntity<PagedResponse<BeneficiaryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(criteria),
                    new ParameterizedTypeReference<PagedResponse<BeneficiaryResponse>>() {}
            );
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setPagedBeneficiaries(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }
}
