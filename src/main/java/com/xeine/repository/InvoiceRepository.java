package com.xeine.repository;

import com.xeine.models.Invoice;
import com.xeine.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Find invoice by ID and company
    @Query("SELECT i FROM Invoice i WHERE i.id = :invoiceId AND i.company.companyId = :companyId")
    Optional<Invoice> findActiveByIdAndCompany(@Param("invoiceId") Long invoiceId, @Param("companyId") Long companyId);

    // Find all invoices by company
    List<Invoice> findByCompanyCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find invoice by quotation ID
    Optional<Invoice> findByQuotationQuotationIdAndCompanyCompanyId(Long quotationId, Long companyId);

    // Find invoices by customer
    @Query("SELECT i FROM Invoice i WHERE i.customer.customerId = :customerId AND i.company.companyId = :companyId ORDER BY i.createdAt DESC")
    List<Invoice> findByCustomerCustomerIdAndCompanyCompanyIdOrderByCreatedAtDesc(Long customerId, Long companyId);

    // Find invoices by status
    List<Invoice> findByStatusAndCompanyCompanyIdOrderByCreatedAtDesc(InvoiceStatus status, Long companyId);


    boolean existsByInvoiceNumberAndCompanyCompanyId(String invoiceNumber, Long companyId);
}