package com.alok.payment.beneficiaries.repository;

import com.alok.payment.beneficiaries.model.Beneficiary;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends CrudRepository<Beneficiary, Long> {
    
    @Query("SELECT * FROM beneficiaries WHERE customer_id = :customerId AND status = 'ACTIVE'")
    List<Beneficiary> findByCustomerId(@Param("customerId") String customerId);
    
    @Query("SELECT * FROM beneficiaries WHERE customer_id = :customerId " +
           "AND (:accountNumber IS NULL OR account_number = :accountNumber) " +
           "AND status = 'ACTIVE'")
    List<Beneficiary> findByCustomerIdAndAccountNumber(
            @Param("customerId") String customerId, 
            @Param("accountNumber") String accountNumber);
    
    @Query("SELECT * FROM beneficiaries WHERE id = :id AND customer_id = :customerId AND status = 'ACTIVE'")
    Optional<Beneficiary> findByIdAndCustomerId(
            @Param("id") Long id, 
            @Param("customerId") String customerId);
    
    @Query("SELECT * FROM beneficiaries WHERE customer_id = :customerId " +
           "AND beneficiary_account_number = :beneficiaryAccountNumber " +
           "AND status = 'ACTIVE'")
    Optional<Beneficiary> findByCustomerIdAndBeneficiaryAccountNumber(
            @Param("customerId") String customerId,
            @Param("beneficiaryAccountNumber") String beneficiaryAccountNumber);
    
    @Modifying
    @Query("UPDATE beneficiaries SET status = 'DELETED' WHERE id = :id AND customer_id = :customerId")
    int softDeleteByIdAndCustomerId(@Param("id") Long id, @Param("customerId") String customerId);
    
    @Modifying
    @Query("DELETE FROM beneficiaries")
    void deleteAll();
}
