
package com.xeine.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotationCreateRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Quotation date is required")
    private LocalDate quotationDate;

    @DecimalMin(value = "0.0", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage;

    @Size(max = 500, message = "Discount reason must not exceed 500 characters")
    private String discountReason;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @NotNull(message = "At least one quotation item is required")
    @Size(min = 1, message = "At least one quotation item is required")
    @Valid
    private List<QuotationItemRequest> items;
}