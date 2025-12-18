package com.alok.payment.beneficiaries.bdd.steps;

import com.alok.payment.beneficiaries.bdd.config.CucumberSpringConfiguration;
import com.alok.payment.beneficiaries.bdd.context.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyticsSteps {

    @Autowired
    private CucumberSpringConfiguration springConfiguration;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @When("I request analytics for customer {string}")
    public void iRequestAnalyticsForCustomer(String customerId) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/analytics?customerId=" + customerId;
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setAnalyticsData(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("I request duplicate detection for customer {string}")
    public void iRequestDuplicateDetectionForCustomer(String customerId) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/duplicates?customerId=" + customerId;
        
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setDuplicatesData(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @When("I request usage report for customer {string} from {string} to {string}")
    public void iRequestUsageReportForCustomer(String customerId, String startDate, String endDate) {
        String url = "http://localhost:" + springConfiguration.getPort() + 
                    "/api/v1/beneficiaries/usage-report?customerId=" + customerId +
                    "&startDate=" + startDate + "&endDate=" + endDate;
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            testContext.setLastResponse(response);
            if (response.getBody() != null) {
                testContext.setUsageReportData(response.getBody());
            }
        } catch (Exception e) {
            testContext.setLastException(e);
        }
    }

    @And("the analytics should show total beneficiaries is {int}")
    public void theAnalyticsShouldShowTotalBeneficiariesIs(int expectedCount) {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        assertThat(analytics.get("totalBeneficiaries")).isEqualTo(expectedCount);
    }

    @And("the analytics should show active beneficiaries is {int}")
    public void theAnalyticsShouldShowActiveBeneficiariesIs(int expectedCount) {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        Object activeBeneficiaries = analytics.get("activeBeneficiaries");
        if (activeBeneficiaries instanceof Long) {
            assertThat((Long) activeBeneficiaries).isEqualTo((long) expectedCount);
        } else {
            assertThat(activeBeneficiaries).isEqualTo(expectedCount);
        }
    }

    @And("the analytics should show inactive beneficiaries is {int}")
    public void theAnalyticsShouldShowInactiveBeneficiariesIs(int expectedCount) {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        Object inactiveBeneficiaries = analytics.get("inactiveBeneficiaries");
        if (inactiveBeneficiaries instanceof Long) {
            assertThat((Long) inactiveBeneficiaries).isEqualTo((long) expectedCount);
        } else {
            assertThat(inactiveBeneficiaries).isEqualTo(expectedCount);
        }
    }

    @And("the analytics should include beneficiaries by type:")
    public void theAnalyticsShouldIncludeBeneficiariesByType(io.cucumber.datatable.DataTable dataTable) {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        assertThat(analytics).containsKey("beneficiariesByType");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> byType = (Map<String, Object>) analytics.get("beneficiariesByType");
        assertThat(byType).isNotNull();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String type = row.get("type");
            int expectedCount = Integer.parseInt(row.get("count"));
            assertThat(byType).containsKey(type);
            assertThat(((Number) byType.get(type)).intValue()).isEqualTo(expectedCount);
        }
    }

    @And("the analytics should include beneficiaries by bank:")
    public void theAnalyticsShouldIncludeBeneficiariesByBank(io.cucumber.datatable.DataTable dataTable) {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        assertThat(analytics).containsKey("beneficiariesByBank");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> byBank = (Map<String, Object>) analytics.get("beneficiariesByBank");
        assertThat(byBank).isNotNull();
        
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String bankName = row.get("bankName");
            int expectedCount = Integer.parseInt(row.get("count"));
            assertThat(byBank).containsKey(bankName);
            assertThat(((Number) byBank.get(bankName)).intValue()).isEqualTo(expectedCount);
        }
    }

    @And("the analytics should include most recent beneficiary name")
    public void theAnalyticsShouldIncludeMostRecentBeneficiaryName() {
        Map<String, Object> analytics = testContext.getAnalyticsData();
        assertThat(analytics).isNotNull();
        assertThat(analytics).containsKey("mostRecentBeneficiaryName");
        assertThat(analytics.get("mostRecentBeneficiaryName")).isNotNull();
        assertThat(analytics).containsKey("mostRecentAddedAt");
    }

    @And("the duplicate results should contain at least {int} potential duplicate pair(s)")
    public void theDuplicateResultsShouldContainAtLeastPotentialDuplicatePairs(int minCount) {
        List<Map<String, Object>> duplicates = testContext.getDuplicatesData();
        assertThat(duplicates).isNotNull();
        assertThat(duplicates.size()).isGreaterThanOrEqualTo(minCount);
    }

    @And("the duplicate results should contain {int} potential duplicate pairs")
    public void theDuplicateResultsShouldContainPotentialDuplicatePairs(int expectedCount) {
        List<Map<String, Object>> duplicates = testContext.getDuplicatesData();
        assertThat(duplicates).isNotNull();
        assertThat(duplicates.size()).isEqualTo(expectedCount);
    }

    @And("the first duplicate pair should include beneficiary names {string} and {string}")
    public void theFirstDuplicatePairShouldIncludeBeneficiaryNamesAnd(String name1, String name2) {
        List<Map<String, Object>> duplicates = testContext.getDuplicatesData();
        assertThat(duplicates).isNotEmpty();
        
        Map<String, Object> firstPair = duplicates.get(0);
        assertThat(firstPair).containsKey("beneficiary1Name");
        assertThat(firstPair).containsKey("beneficiary2Name");
        
        String actualName1 = (String) firstPair.get("beneficiary1Name");
        String actualName2 = (String) firstPair.get("beneficiary2Name");
        
        boolean namesMatch = (actualName1.equals(name1) && actualName2.equals(name2)) ||
                             (actualName1.equals(name2) && actualName2.equals(name1));
        assertThat(namesMatch).isTrue();
    }

    @And("the usage report should show customerId {string}")
    public void theUsageReportShouldShowCustomerId(String expectedCustomerId) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report.get("customerId")).isEqualTo(expectedCustomerId);
    }

    @And("the usage report should include report period start {string}")
    public void theUsageReportShouldIncludeReportPeriodStart(String expectedStart) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report).containsKey("reportPeriodStart");
        
        Object startDate = report.get("reportPeriodStart");
        if (startDate instanceof String) {
            assertThat(startDate).isEqualTo(expectedStart);
        } else if (startDate instanceof LocalDateTime) {
            assertThat(startDate.toString()).startsWith(expectedStart.substring(0, 10));
        }
    }

    @And("the usage report should include report period end {string}")
    public void theUsageReportShouldIncludeReportPeriodEnd(String expectedEnd) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report).containsKey("reportPeriodEnd");
        
        Object endDate = report.get("reportPeriodEnd");
        if (endDate instanceof String) {
            assertThat(endDate).isEqualTo(expectedEnd);
        } else if (endDate instanceof LocalDateTime) {
            assertThat(endDate.toString()).startsWith(expectedEnd.substring(0, 10));
        }
    }

    @And("the usage report should show beneficiaries added in period")
    public void theUsageReportShouldShowBeneficiariesAddedInPeriod() {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report).containsKey("beneficiariesAddedInPeriod");
    }

    @And("the usage report should show beneficiaries added in period is {int}")
    public void theUsageReportShouldShowBeneficiariesAddedInPeriodIs(int expectedCount) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report.get("beneficiariesAddedInPeriod")).isEqualTo(expectedCount);
    }

    @And("the usage report should include growth rate percent")
    public void theUsageReportShouldIncludeGrowthRatePercent() {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report).containsKey("growthRatePercent");
    }

    @And("the usage report should show total beneficiaries is {int}")
    public void theUsageReportShouldShowTotalBeneficiariesIs(int expectedCount) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report.get("totalBeneficiaries")).isEqualTo(expectedCount);
    }

    @And("the usage report should show growth rate percent is {double}")
    public void theUsageReportShouldShowGrowthRatePercentIs(double expectedRate) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        assertThat(report.get("growthRatePercent")).isEqualTo(expectedRate);
    }

    @And("the usage report should show growth rate percent is greater than {int}")
    public void theUsageReportShouldShowGrowthRatePercentIsGreaterThan(int minRate) {
        Map<String, Object> report = testContext.getUsageReportData();
        assertThat(report).isNotNull();
        Object growthRate = report.get("growthRatePercent");
        
        double rate;
        if (growthRate instanceof Integer) {
            rate = ((Integer) growthRate).doubleValue();
        } else {
            rate = (Double) growthRate;
        }
        
        assertThat(rate).isGreaterThan((double) minRate);
    }
}
