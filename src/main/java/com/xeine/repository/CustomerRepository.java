package com.xeine.repository;

import com.xeine.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {

    // FIXED: Updated to match your new entity field names
    @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId AND c.company.companyId = :companyId")
    Optional<Customer> findByCustomerIdAndCompanyCompanyId(@Param("customerId") Long customerId, @Param("companyId") Long companyId);

    /**
     * Find customers by company ID
     */
    List<Customer> findByCompanyCompanyId(Long companyId);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by mobile
     */
    Optional<Customer> findByMobile(String mobile);

    /**
     * Check if customer exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if customer exists by mobile
     */
    boolean existsByMobile(String mobile);

    /**
     * Count customers by company ID
     */
    long countByCompanyCompanyId(Long companyId);

    /**
     * Find customers by GST number
     */
    List<Customer> findByGstNumber(String gstNumber);

    /**
     * Find customers created between dates
     */
    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Customer> findByCreatedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Find customers by company and search term
     */
    @Query("SELECT c FROM Customer c WHERE c.company.companyId = :companyId " +
            "AND (LOWER(c.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findByCompanyAndSearchTerm(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm);

    /**
     * Check if email exists for different customer (useful for updates)
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email AND c.customerId != :customerId")
    boolean existsByEmailAndNotCustomerId(@Param("email") String email, @Param("customerId") Long customerId);

    /**
     * Check if mobile exists for different customer (useful for updates)
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.mobile = :mobile AND c.customerId != :customerId")
    boolean existsByMobileAndNotCustomerId(@Param("mobile") String mobile, @Param("customerId") Long customerId);

    // ADDITIONAL METHODS FOR INVOICE SYSTEM

    /**
     * Find customer by email and company (for invoice creation)
     */
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.company.companyId = :companyId")
    Optional<Customer> findByEmailAndCompany(@Param("email") String email, @Param("companyId") Long companyId);

    /**
     * Find customer by mobile and company
     */
    @Query("SELECT c FROM Customer c WHERE c.mobile = :mobile AND c.company.companyId = :companyId")
    Optional<Customer> findByMobileAndCompany(@Param("mobile") String mobile, @Param("companyId") Long companyId);

    /**
     * Check if email exists within company scope
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email AND c.company.companyId = :companyId")
    boolean existsByEmailAndCompany(@Param("email") String email, @Param("companyId") Long companyId);

    /**
     * Check if mobile exists within company scope
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.mobile = :mobile AND c.company.companyId = :companyId")
    boolean existsByMobileAndCompany(@Param("mobile") String mobile, @Param("companyId") Long companyId);

    /**
     * Check if GST number exists within company scope
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.gstNumber = :gstNumber AND c.company.companyId = :companyId")
    boolean existsByGstNumberAndCompany(@Param("gstNumber") String gstNumber, @Param("companyId") Long companyId);

    /**
     * Find customer by GST number and company
     */
    @Query("SELECT c FROM Customer c WHERE c.gstNumber = :gstNumber AND c.company.companyId = :companyId")
    Optional<Customer> findByGstNumberAndCompany(@Param("gstNumber") String gstNumber, @Param("companyId") Long companyId);

    /**
     * Enhanced search across multiple fields within company
     */
    @Query("SELECT c FROM Customer c WHERE c.company.companyId = :companyId AND " +
            "(LOWER(c.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.mobile) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> searchCustomersInCompany(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);

    /**
     * Find customers with outstanding invoices
     */
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.invoices i WHERE c.company.companyId = :companyId AND i.outstandingAmount > 0")
    List<Customer> findCustomersWithOutstandingInvoices(@Param("companyId") Long companyId);

    /**
     * Find customers ordered by name within company
     */
    @Query("SELECT c FROM Customer c WHERE c.company.companyId = :companyId ORDER BY c.customerName")
    List<Customer> findByCompanyOrderByName(@Param("companyId") Long companyId);

    /**
     * Find recently created customers within company
     */
    @Query("SELECT c FROM Customer c WHERE c.company.companyId = :companyId ORDER BY c.createdAt DESC")
    List<Customer> findRecentCustomersByCompany(@Param("companyId") Long companyId);

    /**
     * Check uniqueness for updates (excluding current customer)
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email AND c.company.companyId = :companyId AND c.customerId != :customerId")
    boolean existsByEmailAndCompanyExcludingId(@Param("email") String email, @Param("companyId") Long companyId, @Param("customerId") Long customerId);

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.mobile = :mobile AND c.company.companyId = :companyId AND c.customerId != :customerId")
    boolean existsByMobileAndCompanyExcludingId(@Param("mobile") String mobile, @Param("companyId") Long companyId, @Param("customerId") Long customerId);

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.gstNumber = :gstNumber AND c.company.companyId = :companyId AND c.customerId != :customerId")
    boolean existsByGstNumberAndCompanyExcludingId(@Param("gstNumber") String gstNumber, @Param("companyId") Long companyId, @Param("customerId") Long customerId);
}

/*
package com.xeine.repository;

import com.xeine.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {


    @Query("SELECT c FROM Customer c WHERE c.customerId = :customerId AND c.company.companyId = :companyId")
    Optional<Customer> findByCustomer_idAndCompanyCompanyId(@Param("customerId") Long customerId, @Param("companyId") Long companyId);



    */
/**
     * Find customers by company ID
     *//*

    List<Customer> findByCompanyCompanyId(Long companyId);

    */
/**
     * Find customer by email
     *//*

    Optional<Customer> findByEmail(String email);

    */
/**
     * Find customer by mobile
     *//*

    Optional<Customer> findByMobile(String mobile);

    */
/**
     * Check if customer exists by email
     *//*

    boolean existsByEmail(String email);

    */
/**
     * Check if customer exists by mobile
     *//*

    boolean existsByMobile(String mobile);

    */
/**
     * Search customers by name or email (case insensitive)
     *//*

//    List<Customer> findByCustomer_nameContainingIgnoreCaseOrEmailContainingIgnoreCase(String customerName, String email);

    */
/**
     * Count customers by company ID
     *//*

    long countByCompanyCompanyId(Long companyId);

    */
/**
     * Find customers by GST number
     *//*

    List<Customer> findByGstNumber(String gstNumber);

    */
/**
     * Find customers created between dates
     *//*

    @Query("SELECT c FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Customer> findByCreatedAtBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    */
/**
     * Find customers by company and search term
     *//*

    @Query("SELECT c FROM Customer c WHERE c.company.companyId = :companyId " +
            "AND (LOWER(c.customer_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findByCompanyAndSearchTerm(
            @Param("companyId") Long companyId,
            @Param("searchTerm") String searchTerm);

    */
/**
     * Check if email exists for different customer (useful for updates)
     *//*

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email AND c.customer_id != :customerId")
    boolean existsByEmailAndNotCustomerId(@Param("email") String email, @Param("customerId") Long customerId);

    */
/**
     * Check if mobile exists for different customer (useful for updates)
     *//*

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.mobile = :mobile AND c.customer_id != :customerId")
    boolean existsByMobileAndNotCustomerId(@Param("mobile") String mobile, @Param("customerId") Long customerId);
}*/
