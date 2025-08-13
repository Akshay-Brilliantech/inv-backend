package com.xeine.dto.request;

import com.xeine.enums.QuotationStatus;
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
public class QuotationUpdateRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private LocalDate quotationDate;

    @DecimalMin(value = "0.0", message = "Discount percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    private BigDecimal discountPercentage;

    @Size(max = 500, message = "Discount reason must not exceed 500 characters")
    private String discountReason;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private QuotationStatus status;

    @Valid
    private List<QuotationItemRequest> items;
}