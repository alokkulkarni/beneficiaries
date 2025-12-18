package com.alok.payment.beneficiaries.bdd.context;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import com.alok.payment.beneficiaries.dto.PagedResponse;
import com.alok.payment.beneficiaries.model.Beneficiary;
import com.alok.payment.beneficiaries.service.BeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestContext {
    
    @Autowired
    private BeneficiaryService beneficiaryService;
    
    private BeneficiaryRequest currentRequest;
    private ResponseEntity<?> lastResponse;
    private BeneficiaryResponse currentBeneficiary;
    private List<BeneficiaryResponse> beneficiaryList;
    private Map<String, Object> testData = new HashMap<>();
    private Exception lastException;
    private PagedResponse<Beneficiary> searchResponse;
    private Map<String, Object> analyticsResponse;
    private List<Map<String, Object>> duplicatesResponse;
    private Map<String, Object> usageReportResponse;
    
    public void reset() {
        currentRequest = null;
        lastResponse = null;
        currentBeneficiary = null;
        beneficiaryList = null;
        testData.clear();
        lastException = null;
        searchResponse = null;
        analyticsResponse = null;
        duplicatesResponse = null;
        usageReportResponse = null;
    }
    
    public BeneficiaryRequest getCurrentRequest() {
        return currentRequest;
    }
    
    public void setCurrentRequest(BeneficiaryRequest currentRequest) {
        this.currentRequest = currentRequest;
    }
    
    public ResponseEntity<?> getLastResponse() {
        return lastResponse;
    }
    
    public void setLastResponse(ResponseEntity<?> lastResponse) {
        this.lastResponse = lastResponse;
    }
    
    public BeneficiaryResponse getCurrentBeneficiary() {
        return currentBeneficiary;
    }
    
    public void setCurrentBeneficiary(BeneficiaryResponse currentBeneficiary) {
        this.currentBeneficiary = currentBeneficiary;
    }
    
    public List<BeneficiaryResponse> getBeneficiaryList() {
        return beneficiaryList;
    }
    
    public void setBeneficiaryList(List<BeneficiaryResponse> beneficiaryList) {
        this.beneficiaryList = beneficiaryList;
    }
    
    public void setTestData(String key, Object value) {
        testData.put(key, value);
    }
    
    public Object getTestData(String key) {
        return testData.get(key);
    }
    
    public Exception getLastException() {
        return lastException;
    }
    
    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
    
    public BeneficiaryService getBeneficiaryService() {
        return beneficiaryService;
    }
    
    public PagedResponse<Beneficiary> getSearchResponse() {
        return searchResponse;
    }
    
    public void setSearchResponse(PagedResponse<Beneficiary> searchResponse) {
        this.searchResponse = searchResponse;
    }
    
    public Map<String, Object> getAnalyticsResponse() {
        return analyticsResponse;
    }
    
    public void setAnalyticsResponse(Map<String, Object> analyticsResponse) {
        this.analyticsResponse = analyticsResponse;
    }
    
    public List<Map<String, Object>> getDuplicatesResponse() {
        return duplicatesResponse;
    }
    
    public void setDuplicatesResponse(List<Map<String, Object>> duplicatesResponse) {
        this.duplicatesResponse = duplicatesResponse;
    }
    
    public Map<String, Object> getUsageReportResponse() {
        return usageReportResponse;
    }
    
    public void setUsageReportResponse(Map<String, Object> usageReportResponse) {
        this.usageReportResponse = usageReportResponse;
    }
}
