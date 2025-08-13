package com.xeine.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderCreateRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "PO number is required")
    @Size(max = 100, message = "PO number must not exceed 100 characters")
    private String poNumber;

    @NotBlank(message = "Vendor name is required")
    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    private String vendorName;

    @NotNull(message = "PO date is required")
    private LocalDate poDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<PurchaseOrderItemRequest> items;
}