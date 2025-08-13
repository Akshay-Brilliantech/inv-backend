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
public class DeliveryChallanResponseDTO {

    private Long deliveryChallanId;
    private String challanNumber;

    // Invoice basic info
    private Long invoiceId;
    private String invoiceNumber;

    // Company info (minimal)
    private Long companyId;
    private String companyName;
    private String companyAddress;
    private String companyGstNumber;

    // Customer info (minimal)
    private Long customerId;
    private String customerName;
    private String customerAddress;
    private String customerGstNumber;

    // Delivery challan specific fields
    private LocalDate deliveryDate;
    private String paymentMode;
    private String notes;
    private String attachmentUrl;
    private String attachmentName;
    private String createdBy;

    // Financial totals
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // Items
    private List<DeliveryChallanItemResponseDTO> items;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional computed fields
    private Integer totalItems;
}