package com.xeine.dto.response;


import com.xeine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuotationItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private BusinessType productType;
    private String productImage;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private String description;
}
