package com.xeine.repository;

import com.xeine.models.Settlement;
import com.xeine.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // Find settlements by invoice ID
    List<Settlement> findByInvoiceIdOrderBySettlementDateDesc(Long invoiceId);

    // Find settlement by settlementId and company (FIXED - using @Query)
    @Query("SELECT s FROM Settlement s WHERE s.settlementId = :settlementId AND s.invoice.company.companyId = :companyId")
    Optional<Settlement> findBySettlementIdAndCompany(@Param("settlementId") Long settlementId, @Param("companyId") Long companyId);

    // Find settlements by company
    List<Settlement> findByInvoiceCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find settlements by date range
    List<Settlement> findByInvoiceCompanyCompanyIdAndSettlementDateBetweenOrderBySettlementDateDesc(
            Long companyId, LocalDate startDate, LocalDate endDate);

    // Find settlements by payment method
    List<Settlement> findByInvoiceCompanyCompanyIdAndPaymentMethodOrderByCreatedAtDesc(
            Long companyId, PaymentMethod paymentMethod);

    // Get total settlements amount by company
    @Query("SELECT SUM(s.amountPaid) FROM Settlement s WHERE s.invoice.company.companyId = :companyId")
    BigDecimal getTotalSettlementsAmountByCompany(@Param("companyId") Long companyId);

    // Get total settlements amount by company and payment method
    @Query("SELECT SUM(s.amountPaid) FROM Settlement s WHERE s.invoice.company.companyId = :companyId AND s.paymentMethod = :paymentMethod")
    BigDecimal getTotalSettlementsAmountByCompanyAndPaymentMethod(
            @Param("companyId") Long companyId, @Param("paymentMethod") PaymentMethod paymentMethod);

    // Get settlements count by invoice
    Long countByInvoiceId(Long invoiceId);

    // Find recent settlements
    @Query("SELECT s FROM Settlement s WHERE s.invoice.company.companyId = :companyId AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<Settlement> findRecentSettlements(@Param("companyId") Long companyId, @Param("since") LocalDate since);
}