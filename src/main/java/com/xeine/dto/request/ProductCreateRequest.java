package com.xeine.dto.request;
import com.xeine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String productName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 20, message = "HSN code must not exceed 20 characters")
    private String hsnCode;

    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    private String barcode;

    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    private String unitOfMeasure;

    // SIMPLE STRING CATEGORIES
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 100, message = "Subcategory must not exceed 100 characters")
    private String subcategory;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid selling price format")
    private BigDecimal sellingPrice;

    @NotNull(message = "Product type is required")
    private BusinessType productType;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid tax rate format")
    private BigDecimal taxRate;

    private String productImageUrl;
    private MultipartFile productImageFile;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}
/*
package com.xeine.dto.request;

import com.xeine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String productName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid selling price format")
    private BigDecimal sellingPrice;

    @NotNull(message = "Product type is required")
    private BusinessType productType;

    // Stock quantity - only for products, ignored for services
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid tax rate format")
    private BigDecimal taxRate;

    // Image fields
    private String productImageUrl; // For URL-based images
    private MultipartFile productImageFile; // For file uploads

    @NotNull(message = "Company ID is required")
    private Long companyId;
}*/
