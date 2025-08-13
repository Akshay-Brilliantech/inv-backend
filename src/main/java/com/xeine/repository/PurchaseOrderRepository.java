package com.xeine.repository;

import com.xeine.models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Basic queries (automatically exclude soft deleted due to @Where clause)
    @Query("SELECT po FROM PurchaseOrder po WHERE po.purchaseOrderId = :poId AND po.company.companyId = :companyId")
    Optional<PurchaseOrder> findByIdAndCompany(@Param("poId") Long poId, @Param("companyId") Long companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.company.companyId = :companyId ORDER BY po.createdAt DESC")
    List<PurchaseOrder> findAllByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.poNumber = :poNumber")
    Optional<PurchaseOrder> findByPoNumber(@Param("poNumber") String poNumber);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.poNumber = :poNumber AND po.company.companyId = :companyId")
    Optional<PurchaseOrder> findByPoNumberAndCompanyId(@Param("poNumber") String poNumber, @Param("companyId") Long companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.vendorName LIKE %:vendorName% AND po.company.companyId = :companyId ORDER BY po.createdAt DESC")
    List<PurchaseOrder> findByVendorNameContainingAndCompanyId(@Param("vendorName") String vendorName, @Param("companyId") Long companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.poDate BETWEEN :startDate AND :endDate AND po.company.companyId = :companyId ORDER BY po.poDate DESC")
    List<PurchaseOrder> findByPoDateBetweenAndCompanyId(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("companyId") Long companyId);

    // Soft delete methods
    @Modifying
    @Query("UPDATE PurchaseOrder po SET po.deleted = true WHERE po.purchaseOrderId = :poId AND po.company.companyId = :companyId")
    int softDeleteByIdAndCompany(@Param("poId") Long poId, @Param("companyId") Long companyId);

    @Modifying
    @Query("UPDATE PurchaseOrder po SET po.deleted = false WHERE po.purchaseOrderId = :poId AND po.company.companyId = :companyId")
    int restoreByIdAndCompany(@Param("poId") Long poId, @Param("companyId") Long companyId);

    // Check existence methods
    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM PurchaseOrder po WHERE po.poNumber = :poNumber")
    boolean existsByPoNumber(@Param("poNumber") String poNumber);

    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM PurchaseOrder po WHERE po.poNumber = :poNumber AND po.company.companyId = :companyId")
    boolean existsByPoNumberAndCompanyId(@Param("poNumber") String poNumber, @Param("companyId") Long companyId);

    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM PurchaseOrder po WHERE po.poNumber = :poNumber AND po.purchaseOrderId != :excludeId")
    boolean existsByPoNumberAndIdNot(@Param("poNumber") String poNumber, @Param("excludeId") Long excludeId);

    // Count methods
    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.company.companyId = :companyId AND po.deleted = false")
    long countActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.company.companyId = :companyId AND po.deleted = true")
    long countDeletedByCompanyId(@Param("companyId") Long companyId);

    // Recent POs with pagination
    List<PurchaseOrder> findTop10ByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<PurchaseOrder> findTop5ByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);
}