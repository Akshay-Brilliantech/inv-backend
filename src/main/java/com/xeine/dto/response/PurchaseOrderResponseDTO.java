package com.xeine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderResponseDTO {

    private Long purchaseOrderId;
    private String poNumber;

    // Company info (minimal)
    private Long companyId;
    private String companyName;

    // Vendor info
    private String vendorName;

    // Date
    private LocalDate poDate;

    // Financial total
    private BigDecimal totalAmount;

    // Metadata
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Items
    private List<PurchaseOrderItemResponseDTO> items;

    // Computed fields
    private Integer totalItems;
}