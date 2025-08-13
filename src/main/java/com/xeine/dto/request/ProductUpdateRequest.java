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
public class ProductUpdateRequest {

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

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 100, message = "Subcategory must not exceed 100 characters")
    private String subcategory;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid selling price format")
    private BigDecimal sellingPrice;

    private BusinessType productType;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid tax rate format")
    private BigDecimal taxRate;

    private String productImageUrl;
    private MultipartFile productImageFile;
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
public class ProductUpdateRequest {

    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String productName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Cost price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid cost price format")
    private BigDecimal costPrice;

    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid selling price format")
    private BigDecimal sellingPrice;

    private BusinessType productType;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid tax rate format")
    private BigDecimal taxRate;

    // Image fields
    private String productImageUrl;
    private MultipartFile productImageFile;
}
*/
