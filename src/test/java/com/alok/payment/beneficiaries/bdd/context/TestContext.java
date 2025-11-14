package com.alok.payment.beneficiaries.bdd.context;

import com.alok.payment.beneficiaries.dto.BeneficiaryRequest;
import com.alok.payment.beneficiaries.dto.BeneficiaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestContext {
    
    private BeneficiaryRequest currentRequest;
    private ResponseEntity<?> lastResponse;
    private BeneficiaryResponse currentBeneficiary;
    private List<BeneficiaryResponse> beneficiaryList;
    private Map<String, Object> testData = new HashMap<>();
    private Exception lastException;
    
    public void reset() {
        currentRequest = null;
        lastResponse = null;
        currentBeneficiary = null;
        beneficiaryList = null;
        testData.clear();
        lastException = null;
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
}
