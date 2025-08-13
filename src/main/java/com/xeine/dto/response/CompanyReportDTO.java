package com.xeine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyReportDTO {
    private Long companyId;
    private String companyName;
    private String contactEmail;
    private String contactPhone;
    private String address;

    private Integer totalCustomers;
    private Integer totalProducts;
    private Integer totalServices;
    private Integer totalQuotations;
    private Integer totalInvoices;
    private Integer totalSettlements;

    private BigDecimal totalRevenue;
    private BigDecimal outstandingPayments;
    private BigDecimal totalDiscounts;
    private BigDecimal totalTaxCollected;

    private Double quotationToInvoiceConversionRate;
    private Map<String, Integer> invoiceStatusBreakdown;
    private Map<String, BigDecimal> monthlyRevenueTrend;
    private Map<String, Integer> newCustomersTrend;
    private Map<String, Integer> newProductsTrend;
    private List<String> topSellingProducts;
    private List<String> topCustomers;
    private LocalDate lastInvoiceDate;
    private LocalDate lastCustomerAddedDate;
    private String notes;
}
