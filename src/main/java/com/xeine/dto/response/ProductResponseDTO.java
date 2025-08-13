package com.xeine.dto.response;

import com.xeine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private Long productId;
    private String productName;
    private String description;
    private String hsnCode;
    private String barcode;
    private String unitOfMeasure;

    // SIMPLE STRING CATEGORIES
    private String category;
    private String subcategory;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BusinessType productType;
    private Integer stockQuantity;
    private BigDecimal taxRate;
    private String productImage;
    private Long companyId;
    private String companyName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal profitMargin;
    private BigDecimal profitAmount;
    //private String stockStatus;
    //private String expiryStatus;
    //private Integer daysToExpiry;

    // Reporting fields
    private Integer totalSold;
    private BigDecimal totalRevenue;
    //private LocalDate lastSoldDate;
}

/*
package com.xeine.dto.response;

import com.xeine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BusinessType productType;
    private Integer stockQuantity; // null for services
    private BigDecimal taxRate;
    private String productImage;
    private Long companyId;
    private String companyName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal profitMargin; // (selling - cost) / cost * 100
    private BigDecimal profitAmount; // selling - cost
    private String stockStatus; // "In Stock", "Low Stock", "Out of Stock", "N/A" (for services)
}*/
