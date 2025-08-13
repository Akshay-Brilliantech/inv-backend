package com.xeine.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Ordered quantity is required")
    @DecimalMin(value = "0.01", message = "Ordered quantity must be greater than 0")
    private BigDecimal orderedQuantity;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
