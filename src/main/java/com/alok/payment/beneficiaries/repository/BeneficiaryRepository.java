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
    
    @Query("SELECT * FROM beneficiaries WHERE customer_id = :customerId")
    List<Beneficiary> findAllByCustomerId(@Param("customerId") String customerId);
    
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
    
    @Query("SELECT * FROM beneficiaries WHERE " +
           "(:customerId IS NULL OR customer_id = :customerId) " +
           "AND (:beneficiaryName IS NULL OR LOWER(beneficiary_name) LIKE LOWER(CONCAT('%', :beneficiaryName, '%'))) " +
           "AND (:beneficiaryType IS NULL OR beneficiary_type = :beneficiaryType) " +
           "AND (:status IS NULL OR status = :status) " +
           "AND (:beneficiaryBankCode IS NULL OR beneficiary_bank_code = :beneficiaryBankCode) " +
           "AND (:createdAfter::timestamp IS NULL OR created_at >= :createdAfter) " +
           "AND (:createdBefore::timestamp IS NULL OR created_at <= :createdBefore) " +
           "ORDER BY id DESC " +
           "LIMIT :limit OFFSET :offset")
    List<Beneficiary> searchBeneficiaries(
            @Param("customerId") String customerId,
            @Param("beneficiaryName") String beneficiaryName,
            @Param("beneficiaryType") String beneficiaryType,
            @Param("status") String status,
            @Param("beneficiaryBankCode") String beneficiaryBankCode,
            @Param("createdAfter") java.time.LocalDateTime createdAfter,
            @Param("createdBefore") java.time.LocalDateTime createdBefore,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            @Param("limit") int limit,
            @Param("offset") int offset);
    
    @Query("SELECT COUNT(*) FROM beneficiaries WHERE " +
           "(:customerId IS NULL OR customer_id = :customerId) " +
           "AND (:beneficiaryName IS NULL OR LOWER(beneficiary_name) LIKE LOWER(CONCAT('%', :beneficiaryName, '%'))) " +
           "AND (:beneficiaryType IS NULL OR beneficiary_type = :beneficiaryType) " +
           "AND (:status IS NULL OR status = :status) " +
           "AND (:beneficiaryBankCode IS NULL OR beneficiary_bank_code = :beneficiaryBankCode) " +
           "AND (:createdAfter::timestamp IS NULL OR created_at >= :createdAfter) " +
           "AND (:createdBefore::timestamp IS NULL OR created_at <= :createdBefore)")
    long countBeneficiaries(
            @Param("customerId") String customerId,
            @Param("beneficiaryName") String beneficiaryName,
            @Param("beneficiaryType") String beneficiaryType,
            @Param("status") String status,
            @Param("beneficiaryBankCode") String beneficiaryBankCode,
            @Param("createdAfter") java.time.LocalDateTime createdAfter,
            @Param("createdBefore") java.time.LocalDateTime createdBefore);
}
