package com.xeine.repository;

import com.xeine.enums.QuotationStatus;
import com.xeine.models.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {


    @Query("SELECT q FROM Quotation q WHERE q.quotationId = :quotationId AND q.company.companyId = :companyId")
    Optional<Quotation> findActiveByIdAndCompany(@Param("quotationId") Long quotationId, @Param("companyId") Long companyId);

    List<Quotation> findByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);


   // List<Quotation> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT q FROM Quotation q WHERE q.customer.customerId = ?1 ORDER BY q.createdAt DESC")
    List<Quotation> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Modifying
    @Query("UPDATE Quotation q SET q.deleted = true WHERE q.quotationId = :quotationId AND q.company.companyId = :companyId")
    int softDeleteByIdAndCompany(@Param("quotationId") Long quotationId, @Param("companyId") Long companyId);




    @Query("SELECT q FROM Quotation q WHERE q.quotationId = :quotationId AND q.company.companyId = :companyId")
    Optional<Quotation> findByIdAndCompany(@Param("quotationId") Long quotationId, @Param("companyId") Long companyId);


    List<Quotation> findByStatusAndCompanyCompanyIdOrderByCreatedAtDesc(QuotationStatus status, Long companyId);


    @Query("SELECT q FROM Quotation q WHERE q.status = 'DRAFT' AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> findDraftQuotationsByCompany(@Param("companyId") Long companyId);


    @Query("SELECT q FROM Quotation q WHERE q.status = 'SENT' AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> findSentQuotationsByCompany(@Param("companyId") Long companyId);


    @Query("SELECT q FROM Quotation q WHERE q.status = 'APPROVED' AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> findApprovedQuotationsByCompany(@Param("companyId") Long companyId);


    @Query("SELECT q FROM Quotation q WHERE q.status = 'CONVERTED' AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> findConvertedQuotationsByCompany(@Param("companyId") Long companyId);


    @Query("SELECT q FROM Quotation q WHERE LOWER(q.quotationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> searchByQuotationNumber(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);

    // Search by customer name
    @Query("SELECT q FROM Quotation q WHERE LOWER(q.customer.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> searchByCustomerName(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);


    @Query("SELECT q FROM Quotation q WHERE q.quotationDate BETWEEN :startDate AND :endDate AND q.company.companyId = :companyId ORDER BY q.quotationDate DESC")
    List<Quotation> findByDateRange(@Param("companyId") Long companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT q FROM Quotation q WHERE DATE(q.quotationDate) = :date AND q.company.companyId = :companyId ORDER BY q.createdAt DESC")
    List<Quotation> findByQuotationDate(@Param("companyId") Long companyId, @Param("date") LocalDate date);




    @Query("SELECT q FROM Quotation q WHERE q.totalAmount BETWEEN :minAmount AND :maxAmount AND q.company.companyId = :companyId ORDER BY q.totalAmount DESC")
    List<Quotation> findByAmountRange(@Param("companyId") Long companyId, @Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);


    @Query("SELECT q FROM Quotation q WHERE q.discountAmount > 0 AND q.company.companyId = :companyId ORDER BY q.discountAmount DESC")
    List<Quotation> findQuotationsWithDiscount(@Param("companyId") Long companyId);


    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.status = :status AND q.company.companyId = :companyId")
    Long countByStatusAndCompany(@Param("status") QuotationStatus status, @Param("companyId") Long companyId);

    // Get total quotation value
    @Query("SELECT COALESCE(SUM(q.totalAmount), 0) FROM Quotation q WHERE q.company.companyId = :companyId")
    BigDecimal getTotalQuotationValue(@Param("companyId") Long companyId);

    // Get total discount given
    @Query("SELECT COALESCE(SUM(q.discountAmount), 0) FROM Quotation q WHERE q.company.companyId = :companyId")
    BigDecimal getTotalDiscountGiven(@Param("companyId") Long companyId);

    // Get average quotation amount
    @Query("SELECT COALESCE(AVG(q.totalAmount), 0) FROM Quotation q WHERE q.company.companyId = :companyId")
    BigDecimal getAverageQuotationAmount(@Param("companyId") Long companyId);


    boolean existsByQuotationNumberAndCompanyCompanyId(String quotationNumber, Long companyId);


    @Query("SELECT q FROM Quotation q WHERE q.company.companyId = :companyId ORDER BY q.quotationId DESC")
    List<Quotation> findLatestByCompany(@Param("companyId") Long companyId);
}