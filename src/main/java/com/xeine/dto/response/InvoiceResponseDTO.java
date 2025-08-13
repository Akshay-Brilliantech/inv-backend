package com.xeine.dto.response;

import com.xeine.enums.InvoiceStatus;
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
public class InvoiceResponseDTO {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;

    // Company and Customer
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;

    // Related Quotation
    private Long quotationId;
    private String quotationNumber;

    // Financial Details
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal totalBeforeDiscount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;

    // Status and Notes
    private InvoiceStatus status;
    private String discountReason;
    private String notes;
    private LocalDateTime createdAt;

    // Calculated Fields
    private String paymentStatus; // "Paid", "Unpaid", "Partially Paid"
    private Boolean isOverdue;
    private Integer daysOverdue;

    // Invoice Items
    private List<InvoiceItemResponseDTO> invoiceItems;
}