package com.xeine.repository;

import com.xeine.enums.BusinessType;
import com.xeine.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {



    // Find active products by company
    List<Product> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);

    // Find active product by ID and company
    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.company.companyId = :companyId AND p.isActive = true")
    Optional<Product> findActiveByIdAndCompany(@Param("productId") Long productId, @Param("companyId") Long companyId);

    // Find by product type and company (active only)
    List<Product> findByProductTypeAndCompanyCompanyIdAndIsActiveTrue(BusinessType productType, Long companyId);



    // Find by HSN Code
    List<Product> findByHsnCodeAndCompanyCompanyIdAndIsActiveTrue(String hsnCode, Long companyId);

    // Find by Barcode (unique across company)
    Optional<Product> findByBarcodeAndCompanyCompanyIdAndIsActiveTrue(String barcode, Long companyId);

    // Find by Unit of Measure
    List<Product> findByUnitOfMeasureAndCompanyCompanyIdAndIsActiveTrue(String unitOfMeasure, Long companyId);



    // Find by category
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByCategoryAndCompany(@Param("category") String category, @Param("companyId") Long companyId);

    // Find by subcategory
    @Query("SELECT p FROM Product p WHERE LOWER(p.subcategory) = LOWER(:subcategory) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findBySubcategoryAndCompany(@Param("subcategory") String subcategory, @Param("companyId") Long companyId);

    // Find by category and subcategory
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND LOWER(p.subcategory) = LOWER(:subcategory) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByCategoryAndSubcategoryAndCompany(@Param("category") String category, @Param("subcategory") String subcategory, @Param("companyId") Long companyId);

    // Get distinct categories for a company
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.company.companyId = :companyId AND p.isActive = true AND p.category IS NOT NULL ORDER BY p.category")
    List<String> getDistinctCategoriesByCompany(@Param("companyId") Long companyId);

    // Get distinct subcategories for a category
    @Query("SELECT DISTINCT p.subcategory FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.company.companyId = :companyId AND p.isActive = true AND p.subcategory IS NOT NULL ORDER BY p.subcategory")
    List<String> getDistinctSubcategoriesByCategory(@Param("category") String category, @Param("companyId") Long companyId);

    // Count products by category
    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.company.companyId = :companyId AND p.isActive = true GROUP BY p.category")
    List<Object[]> getProductCountByCategory(@Param("companyId") Long companyId);



    // Find products expiring soon (within specified days)
    @Query("SELECT p FROM Product p WHERE p.expiryDate IS NOT NULL AND p.expiryDate BETWEEN CURRENT_DATE AND :expiryDate AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findProductsExpiringSoon(@Param("companyId") Long companyId, @Param("expiryDate") LocalDate expiryDate);

    // Find expired products
    @Query("SELECT p FROM Product p WHERE p.expiryDate IS NOT NULL AND p.expiryDate < CURRENT_DATE AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findExpiredProducts(@Param("companyId") Long companyId);

    // Find products by manufacture date range
    @Query("SELECT p FROM Product p WHERE p.manufactureDate BETWEEN :startDate AND :endDate AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByManufactureDateRange(@Param("companyId") Long companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);



    // Find products only (not services) for a company
    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findProductsByCompany(@Param("companyId") Long companyId);

    // Find services only (not products) for a company
    @Query("SELECT p FROM Product p WHERE p.productType = 'SERVICE' AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findServicesByCompany(@Param("companyId") Long companyId);

    // Find products with low stock
    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND p.stockQuantity <= :threshold AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("companyId") Long companyId, @Param("threshold") Integer threshold);

    // Find out of stock products
    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND (p.stockQuantity IS NULL OR p.stockQuantity = 0) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findOutOfStockProducts(@Param("companyId") Long companyId);



    // Search products by name
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> searchProductsByName(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);

    // Enhanced search including category and subcategory
    @Query("SELECT p FROM Product p WHERE (LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.hsnCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.subcategory) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "p.company.companyId = :companyId AND p.isActive = true")
    List<Product> searchProducts(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);


    // Find products by price range
    @Query("SELECT p FROM Product p WHERE p.sellingPrice BETWEEN :minPrice AND :maxPrice AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByPriceRange(@Param("companyId") Long companyId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Find products with profit margin above threshold
    @Query("SELECT p FROM Product p WHERE ((p.sellingPrice - p.costPrice) / p.costPrice * 100) >= :marginPercentage AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByMinProfitMargin(@Param("companyId") Long companyId, @Param("marginPercentage") BigDecimal marginPercentage);






    // Check if product name exists for company (excluding current product)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:productName) AND p.company.companyId = :companyId AND p.productId != :productId AND p.isActive = true")
    boolean existsByNameAndCompanyExcludingId(@Param("productName") String productName, @Param("companyId") Long companyId, @Param("productId") Long productId);

    // Check if product name exists for company (for new products)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:productName) AND p.company.companyId = :companyId AND p.isActive = true")
    boolean existsByNameAndCompany(@Param("productName") String productName, @Param("companyId") Long companyId);

    // Check if barcode exists for company (excluding current product)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.barcode = :barcode AND p.company.companyId = :companyId AND p.productId != :productId AND p.isActive = true")
    boolean existsByBarcodeAndCompanyExcludingId(@Param("barcode") String barcode, @Param("companyId") Long companyId, @Param("productId") Long productId);

    // Check if barcode exists for company (for new products)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.barcode = :barcode AND p.company.companyId = :companyId AND p.isActive = true")
    boolean existsByBarcodeAndCompany(@Param("barcode") String barcode, @Param("companyId") Long companyId);



    // Find products by tax rate
    @Query("SELECT p FROM Product p WHERE p.taxRate = :taxRate AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByTaxRate(@Param("companyId") Long companyId, @Param("taxRate") BigDecimal taxRate);

    // Group products by HSN code
    @Query("SELECT p.hsnCode, COUNT(p) FROM Product p WHERE p.company.companyId = :companyId AND p.isActive = true GROUP BY p.hsnCode")
    List<Object[]> getProductCountByHsnCode(@Param("companyId") Long companyId);

    // Group products by unit of measure
    @Query("SELECT p.unitOfMeasure, COUNT(p) FROM Product p WHERE p.company.companyId = :companyId AND p.isActive = true GROUP BY p.unitOfMeasure")
    List<Object[]> getProductCountByUnit(@Param("companyId") Long companyId);








    @Query("SELECT p FROM Product p WHERE p.productImage IS NOT NULL AND p.productImage != '' AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findProductsWithImages(@Param("companyId") Long companyId);

    // Find products without images
    @Query("SELECT p FROM Product p WHERE (p.productImage IS NULL OR p.productImage = '') AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findProductsWithoutImages(@Param("companyId") Long companyId);

    // Find products created in date range
    @Query("SELECT p FROM Product p WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByCreatedDateRange(@Param("companyId") Long companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find recently updated products
    @Query("SELECT p FROM Product p WHERE DATE(p.updatedAt) >= :since AND p.company.companyId = :companyId AND p.isActive = true ORDER BY p.updatedAt DESC")
    List<Product> findRecentlyUpdated(@Param("companyId") Long companyId, @Param("since") LocalDate since);

    // ===== BULK OPERATIONS SUPPORT =====

    // Find products by IDs (for bulk operations)
    @Query("SELECT p FROM Product p WHERE p.productId IN :productIds AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByIdsAndCompany(@Param("productIds") List<Long> productIds, @Param("companyId") Long companyId);
}

/*
package com.xeine.repository;

import com.xeine.enums.BusinessType;
import com.xeine.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);


    @Query("SELECT p FROM Product p WHERE p.productId = :productId AND p.company.companyId = :companyId AND p.isActive = true")
    Optional<Product> findActiveByIdAndCompany(@Param("productId") Long productId, @Param("companyId") Long companyId);


    List<Product> findByProductTypeAndCompanyCompanyIdAndIsActiveTrue(BusinessType productType, Long companyId);



    // Find products only (not services) for a company
    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findProductsByCompany(@Param("companyId") Long companyId);

    // Find services only (not products) for a company
    @Query("SELECT p FROM Product p WHERE p.productType = 'SERVICE' AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findServicesByCompany(@Param("companyId") Long companyId);


    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND p.stockQuantity <= :threshold AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("companyId") Long companyId, @Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.productType = 'PRODUCT' AND (p.stockQuantity IS NULL OR p.stockQuantity = 0) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findOutOfStockProducts(@Param("companyId") Long companyId);

    List<Product> findByHsnCodeAndCompanyCompanyIdAndIsActiveTrue(String hsnCode, Long companyId);


    Optional<Product> findByBarcodeAndCompanyCompanyIdAndIsActiveTrue(String barcode, Long companyId);


    List<Product> findByCategoryCategoryIdAndCompanyCompanyIdAndIsActiveTrue(Long categoryId, Long companyId);


    List<Product> findBySubcategorySubcategoryIdAndCompanyCompanyIdAndIsActiveTrue(Long subcategoryId, Long companyId);


    List<Product> findByUnitOfMeasureAndCompanyCompanyIdAndIsActiveTrue(String unitOfMeasure, Long companyId);


    // ===== SEARCH QUERIES =====

    // Search products by name
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> searchProductsByName(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);

    // Search by name and description
    @Query("SELECT p FROM Product p WHERE (LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> searchProducts(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);


    // Find products by price range
    @Query("SELECT p FROM Product p WHERE p.sellingPrice BETWEEN :minPrice AND :maxPrice AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByPriceRange(@Param("companyId") Long companyId, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Find products with profit margin above threshold
    @Query("SELECT p FROM Product p WHERE ((p.sellingPrice - p.costPrice) / p.costPrice * 100) >= :marginPercentage AND p.company.companyId = :companyId AND p.isActive = true")
    List<Product> findByMinProfitMargin(@Param("companyId") Long companyId, @Param("marginPercentage") BigDecimal marginPercentage);



    // Check if product name exists for company (excluding current product)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:productName) AND p.company.companyId = :companyId AND p.productId != :productId AND p.isActive = true")
    boolean existsByNameAndCompanyExcludingId(@Param("productName") String productName, @Param("companyId") Long companyId, @Param("productId") Long productId);

    // Check if product name exists for company (for new products)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.productName) = LOWER(:productName) AND p.company.companyId = :companyId AND p.isActive = true")
    boolean existsByNameAndCompany(@Param("productName") String productName, @Param("companyId") Long companyId);



}*/
