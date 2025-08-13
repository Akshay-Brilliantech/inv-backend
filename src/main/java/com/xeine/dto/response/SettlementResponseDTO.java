package com.xeine.dto.response;


import com.xeine.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementResponseDTO {

    private Long settlementId;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate settlementDate;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String paymentMethodDisplay;
    private String referenceNumber;
    private String notes;
    private LocalDateTime createdAt;

    // Invoice details for context
    private BigDecimal invoiceTotalAmount;
    private BigDecimal invoicePaidAmount;
    private BigDecimal invoiceOutstandingAmount;
    private String invoicePaymentStatus;
}