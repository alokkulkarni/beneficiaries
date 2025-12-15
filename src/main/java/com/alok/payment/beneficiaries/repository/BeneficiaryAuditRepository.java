package com.alok.payment.beneficiaries.repository;

import com.alok.payment.beneficiaries.model.BeneficiaryAudit;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryAuditRepository extends CrudRepository<BeneficiaryAudit, Long> {
    
    List<BeneficiaryAudit> findByBeneficiaryId(Long beneficiaryId);
    
    List<BeneficiaryAudit> findByCustomerId(String customerId);
    
    @Query("SELECT * FROM beneficiary_audits WHERE beneficiary_id = :beneficiaryId AND customer_id = :customerId ORDER BY performed_at DESC")
    List<BeneficiaryAudit> findByBeneficiaryIdAndCustomerId(@Param("beneficiaryId") Long beneficiaryId, 
                                                             @Param("customerId") String customerId);
    
    @Query("SELECT * FROM beneficiary_audits WHERE customer_id = :customerId ORDER BY performed_at DESC")
    List<BeneficiaryAudit> findByCustomerIdOrderByPerformedAtDesc(@Param("customerId") String customerId);
}
