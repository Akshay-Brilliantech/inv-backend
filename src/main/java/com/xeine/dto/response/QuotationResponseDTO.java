package com.xeine.dto.response;


import com.xeine.enums.QuotationStatus;
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
public  class QuotationResponseDTO {

    private Long quotationId;
    private String quotationNumber;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private LocalDate quotationDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal totalBeforeDiscount;
    private BigDecimal totalAmount;
    private QuotationStatus status;
    private String discountReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuotationItemResponseDTO> items;

    // Additional calculated fields
    private Integer totalItems;
    private Boolean hasInvoice;
    private String invoiceNumber;
}