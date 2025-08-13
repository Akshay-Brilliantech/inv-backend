package com.xeine.repository;

import com.xeine.models.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {


    List<QuotationItem> findByQuotationQuotationIdOrderById(Long quotationId);


    void deleteByQuotationQuotationId(Long quotationId);

    @Query("SELECT qi FROM QuotationItem qi WHERE qi.product.productId = :productId")
    List<QuotationItem> findByProductId(@Param("productId") Long productId);
}