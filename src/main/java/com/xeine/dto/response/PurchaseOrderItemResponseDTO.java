package com.xeine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderItemResponseDTO {

    private Long purchaseOrderItemId;

    // Product info
    private Long productId;
    private String productName;
    private String productType;
    private String hsnCode;

    // Quantity and pricing
    private BigDecimal orderedQuantity;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;

    // Additional info
    private String description;
}