package com.xeine.repository;


import com.xeine.models.DeliveryChallan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, Long> {

    // Basic CRUD operations are inherited from JpaRepository

    // Find by challan number
    Optional<DeliveryChallan> findByChallanNumber(String challanNumber);

    // Find by invoice ID
    Optional<DeliveryChallan> findByInvoiceId(Long invoiceId);

    // Find by company (through invoice relationship)
    List<DeliveryChallan> findByInvoice_Company_CompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find by customer (through invoice relationship)
    List<DeliveryChallan> findByInvoice_Customer_CustomerIdOrderByCreatedAtDesc(Long customerId);



    // Find by delivery date
    List<DeliveryChallan> findByDeliveryDateOrderByCreatedAtDesc(LocalDate deliveryDate);

    // Find by delivery date range
    List<DeliveryChallan> findByDeliveryDateBetweenOrderByDeliveryDateDesc(LocalDate startDate, LocalDate endDate);

    // Search by challan number (partial match)
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.challanNumber LIKE %:searchTerm% ORDER BY dc.createdAt DESC")
    List<DeliveryChallan> searchByChallanNumber(@Param("searchTerm") String searchTerm);

    // Search by customer name
    @Query("SELECT dc FROM DeliveryChallan dc WHERE LOWER(dc.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')) ORDER BY dc.createdAt DESC")
    List<DeliveryChallan> searchByCustomerName(@Param("customerName") String customerName);

    // Search by invoice number
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.invoiceNumber LIKE %:invoiceNumber% ORDER BY dc.createdAt DESC")
    List<DeliveryChallan> searchByInvoiceNumber(@Param("invoiceNumber") String invoiceNumber);




    // Check if challan number exists for a company
    @Query("SELECT COUNT(dc) > 0 FROM DeliveryChallan dc WHERE dc.challanNumber = :challanNumber AND dc.invoice.company.companyId = :companyId")
    boolean existsByChallanNumberAndCompany(@Param("challanNumber") String challanNumber, @Param("companyId") Long companyId);

    // Get recent deliveries for a company
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.invoice.company.companyId = :companyId ORDER BY dc.createdAt DESC")
    List<DeliveryChallan> findRecentDeliveriesByCompany(@Param("companyId") Long companyId);

    // Simple search across multiple fields
    @Query("SELECT dc FROM DeliveryChallan dc WHERE " +
            "LOWER(dc.challanNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dc.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dc.invoiceNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY dc.createdAt DESC")
    List<DeliveryChallan> searchDeliveryChallans(@Param("searchTerm") String searchTerm);
}