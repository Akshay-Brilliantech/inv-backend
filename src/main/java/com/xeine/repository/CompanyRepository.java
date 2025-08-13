package com.xeine.repository;

import com.xeine.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("SELECT c FROM Company c WHERE c.mobile = :mobile")
    Optional<Company> findByMobile(@Param("mobile") String mobile);

    Optional<Company> findByEmail(String email);

    Optional<Company> findByMobileOrEmail(String mobile, String email);



    // Find active companies only
    List<Company> findByIsCompanyActiveTrue();

    // Find by email (active only)
    Optional<Company> findByEmailAndIsCompanyActiveTrue(String email);

    // Find by mobile (active only)
    Optional<Company> findByMobileAndIsCompanyActiveTrue(String mobile);

    // Find by company name (active only)
    Optional<Company> findByCompanyNameAndIsCompanyActiveTrue(String companyName);



    // Find by location (active only)
    List<Company> findByCountryAndIsCompanyActiveTrue(String country);
    List<Company> findByStateAndIsCompanyActiveTrue(String state);
    List<Company> findByCityAndIsCompanyActiveTrue(String city);

    // Find by multiple location filters
    List<Company> findByCountryAndStateAndIsCompanyActiveTrue(String country, String state);
    List<Company> findByCountryAndStateAndCityAndIsCompanyActiveTrue(String country, String state, String city);


    // Find verified companies (active only)
    @Query("SELECT c FROM Company c WHERE c.isOtpVerified = true AND c.isCompanyActive = true")
    List<Company> findVerifiedActiveCompanies();



    // Check if email exists excluding current company (active only)
    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.email = :email AND c.companyId != :companyId AND c.isCompanyActive = true")
    boolean existsByEmailAndNotCompanyIdAndActive(@Param("email") String email, @Param("companyId") Long companyId);

    // Check if mobile exists excluding current company (active only)
    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.mobile = :mobile AND c.companyId != :companyId AND c.isCompanyActive = true")
    boolean existsByMobileAndNotCompanyIdAndActive(@Param("mobile") String mobile, @Param("companyId") Long companyId);

    // Check if company name exists excluding current company (active only)
    @Query("SELECT COUNT(c) > 0 FROM Company c WHERE c.companyName = :companyName AND c.companyId != :companyId AND c.isCompanyActive = true")
    boolean existsByCompanyNameAndNotCompanyIdAndActive(@Param("companyName") String companyName, @Param("companyId") Long companyId);

    // Simple existence checks for new companies
    boolean existsByEmailAndIsCompanyActiveTrue(String email);
    boolean existsByMobileAndIsCompanyActiveTrue(String mobile);
    boolean existsByCompanyNameAndIsCompanyActiveTrue(String companyName);



    // Find companies with invoice count greater than specified
    @Query("SELECT c FROM Company c WHERE c.invoiceCount > :count AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithInvoiceCountGreaterThan(@Param("count") Long count);

    // Find companies with specific invoice count
    List<Company> findByInvoiceCountAndIsCompanyActiveTrue(Long invoiceCount);

    // Find companies with invoice count between range
    @Query("SELECT c FROM Company c WHERE c.invoiceCount BETWEEN :minCount AND :maxCount AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesByInvoiceCountBetween(@Param("minCount") Long minCount, @Param("maxCount") Long maxCount);



    // Override findById to include soft delete check (active only)
    @Query("SELECT c FROM Company c WHERE c.companyId = :id AND c.isCompanyActive = true")
    Optional<Company> findActiveById(@Param("id") Long id);

    // Find inactive companies (for admin purposes)
    List<Company> findByIsCompanyActiveFalse();

    // Find all companies regardless of active status (for admin)
    @Query("SELECT c FROM Company c ORDER BY c.createdAt DESC")
    List<Company> findAllCompaniesIncludingInactive();




    @Query("SELECT c FROM Company c WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND c.isCompanyActive = true")
    List<Company> searchActiveCompaniesByName(@Param("searchTerm") String searchTerm);


    @Query("SELECT c FROM Company c WHERE LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND c.isCompanyActive = true")
    List<Company> searchActiveCompaniesByEmail(@Param("searchTerm") String searchTerm);


    @Query("SELECT c FROM Company c WHERE " +
            "(LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.mobile) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND c.isCompanyActive = true")
    List<Company> searchActiveCompanies(@Param("searchTerm") String searchTerm);

    // ===== DATE-BASED QUERIES =====

    // Find companies created after specific date
    @Query("SELECT c FROM Company c WHERE c.createdAt >= :date AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesCreatedAfter(@Param("date") java.time.LocalDateTime date);

    // Find companies created between dates
    @Query("SELECT c FROM Company c WHERE c.createdAt BETWEEN :startDate AND :endDate AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesCreatedBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    // Find recently updated companies
    @Query("SELECT c FROM Company c WHERE c.updatedAt >= :date AND c.isCompanyActive = true ORDER BY c.updatedAt DESC")
    List<Company> findActiveCompaniesUpdatedAfter(@Param("date") java.time.LocalDateTime date);

    // ===== PAGINATION QUERIES =====

    // Find active companies with pagination support (use with Pageable)
    @Query("SELECT c FROM Company c WHERE c.isCompanyActive = true ORDER BY c.createdAt DESC")
    org.springframework.data.domain.Page<Company> findActiveCompaniesPageable(org.springframework.data.domain.Pageable pageable);

    // ===== BULK OPERATIONS =====

    // Update invoice count for specific company
    @Query("UPDATE Company c SET c.invoiceCount = c.invoiceCount + :increment, c.updatedAt = CURRENT_TIMESTAMP WHERE c.companyId = :companyId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int incrementInvoiceCount(@Param("companyId") Long companyId, @Param("increment") Long increment);

    // ===== COMPLEX QUERIES =====

    // Find companies with no customers (active only)
    @Query("SELECT c FROM Company c WHERE c.customers IS EMPTY AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithNoCustomers();

    // Find companies with no products (active only)
    @Query("SELECT c FROM Company c WHERE c.products IS EMPTY AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithNoProducts();

    // Find companies with no invoices (active only)
    @Query("SELECT c FROM Company c WHERE c.invoices IS EMPTY AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithNoInvoices();

    // Find companies by GST number (active only)
    @Query("SELECT c FROM Company c WHERE c.gstNumber = :gstNumber AND c.isCompanyActive = true")
    Optional<Company> findActiveCompanyByGstNumber(@Param("gstNumber") String gstNumber);

    // Find companies without GST number (active only)
    @Query("SELECT c FROM Company c WHERE c.gstNumber IS NULL AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithoutGst();

    // Find companies with GST number (active only)
    @Query("SELECT c FROM Company c WHERE c.gstNumber IS NOT NULL AND c.isCompanyActive = true")
    List<Company> findActiveCompaniesWithGst();
}