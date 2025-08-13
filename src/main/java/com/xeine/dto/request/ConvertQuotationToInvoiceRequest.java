package com.xeine.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConvertQuotationToInvoiceRequest {

    @NotNull(message = "Quotation ID is required")
    private Long quotationId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private LocalDate invoiceDate; // If null, uses current date

    private LocalDate dueDate; // If null, uses current date + 30 days

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}