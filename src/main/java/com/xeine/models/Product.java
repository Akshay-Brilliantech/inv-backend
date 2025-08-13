package com.xeine.models;

import com.xeine.enums.BusinessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @Column(name="product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, name="product_name")
    private String productName;

    @Column(columnDefinition = "TEXT", name="description")
    private String description;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(name = "bar_code", unique = true, length = 50)
    private String barcode;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private BusinessType productType;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    // SIMPLE STRING CATEGORIES
    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public boolean hasSufficientStock(int requiredQuantity) {
        return this.stockQuantity != null && this.stockQuantity >= requiredQuantity;
    }


    public void reduceStock(int quantity) {
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to set default category if empty
    @PrePersist
    public void setDefaults() {
        if (category == null || category.trim().isEmpty()) {
            category = "General";
        }
        if (subcategory == null || subcategory.trim().isEmpty()) {
            subcategory = "Miscellaneous";
        }
    }
}

/*

import com.xeine.enums.BusinessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @Column(name="product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false,name="product_name")
    private String productName;

    @Column(columnDefinition = "TEXT",name="description")
    private String description;

    @Column(name = "hsn_Code")
    private String hsnCode;

    @Column(name = "bar_code", unique = true, length = 50)
    private String barcode;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private BusinessType productType;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;


    @Column(name = "product_image")
    private String productImage;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private  LocalDateTime updatedAt;
}

*/
