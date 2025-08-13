package com.xeine.services;

import com.xeine.dto.response.CompanyReportDTO;
import com.xeine.dto.response.InvoiceItemResponseDTO;
import com.xeine.dto.response.InvoiceResponseDTO;
import com.xeine.dto.response.ProductResponseDTO;
import com.xeine.dto.response.QuotationResponseDTO;
import com.xeine.dto.response.SettlementResponseDTO;
import com.xeine.exception.ReportException;
import com.xeine.models.Invoice;
import com.xeine.models.Product;
import com.xeine.repository.CompanyRepository;
import com.xeine.repository.CustomerRepository;
import com.xeine.repository.InvoiceRepository;
import com.xeine.repository.ProductRepository;
import com.xeine.repository.QuotationRepository;
import com.xeine.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private SettlementRepository settlementRepository;

    public List<InvoiceResponseDTO> getInvoiceReport(Long companyId, String period) {
        var invoices = invoiceRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        if (invoices == null || invoices.isEmpty()) {
            throw new ReportException("No invoices found for the given company.");
        }

        LocalDate startDate = getStartDate(period);
        List<InvoiceResponseDTO> result = invoices.stream()
                .filter(inv -> inv.getInvoiceDate() != null && !inv.getInvoiceDate().isBefore(startDate))
                .map(this::toDto)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new ReportException("No invoices found for the given period.");
        }
        return result;
    }

    private InvoiceResponseDTO toDto(com.xeine.models.Invoice invoice) {
        // Map Invoice model to InvoiceResponseDTO
        System.out.println(invoice);
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setCompanyId(invoice.getCompany().getCompanyId());
        dto.setCompanyName(invoice.getCompany().getCompanyName());
        dto.setCustomerId(invoice.getCustomer().getCustomerId());
        dto.setCustomerName(invoice.getCustomer().getCustomerName());
        dto.setQuotationId(invoice.getQuotation() != null ? invoice.getQuotation().getQuotationId(): null);
        dto.setQuotationNumber(invoice.getQuotation() != null ? invoice.getQuotation().getQuotationNumber() : null);
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountPercentage(invoice.getDiscountPercentage());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalBeforeDiscount(invoice.getTotalBeforeDiscount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setOutstandingAmount(invoice.getOutstandingAmount());
        dto.setStatus(invoice.getStatus());
        dto.setDiscountReason(invoice.getDiscountReason());
        dto.setNotes(invoice.getNotes());
        dto.setCreatedAt(invoice.getCreatedAt());
        // Calculated fields
        dto.setPaymentStatus(calculatePaymentStatus(invoice));
        dto.setIsOverdue(isInvoiceOverdue(invoice));
        dto.setDaysOverdue(getDaysOverdue(invoice));
        // Invoice items
        dto.setInvoiceItems(invoice.getInvoiceItems() != null ? invoice.getInvoiceItems().stream()
            .map(this::toInvoiceItemDto)
                .collect(Collectors.toList()) : null);
        // Settlements can be mapped similarly if needed in future
        return dto;
    }

    private String calculatePaymentStatus(com.xeine.models.Invoice invoice) {
        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) return "Paid";
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) return "Unpaid";
        return "Partially Paid";
    }

    private Boolean isInvoiceOverdue(com.xeine.models.Invoice invoice) {
        return invoice.getDueDate() != null && invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0 && LocalDate.now().isAfter(invoice.getDueDate());
    }

    private Integer getDaysOverdue(com.xeine.models.Invoice invoice) {
        if (isInvoiceOverdue(invoice)) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
        }
        return 0;
    }

    private InvoiceItemResponseDTO toInvoiceItemDto(com.xeine.models.InvoiceItem item) {
        InvoiceItemResponseDTO dto = new InvoiceItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTaxRate(item.getTaxRate());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setLineTotal(item.getLineTotal());
        dto.setDescription(item.getDescription());
        return dto;
    }

    public CompanyReportDTO getCompanyReport(Long companyId) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ReportException("Company not found for ID: " + companyId));
        CompanyReportDTO dto = new CompanyReportDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setContactEmail(company.getEmail());
        dto.setContactPhone(company.getMobile());
        dto.setAddress(company.getAddress());
        // Customers
        int totalCustomers = (int) customerRepository.countByCompanyCompanyId(companyId);
        dto.setTotalCustomers(totalCustomers);
        // Products & Services
        var products = productRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId);
        int totalProducts = (int) products.stream().filter(p -> p.getProductType().name().equalsIgnoreCase("PRODUCT")).count();
        int totalServices = (int) products.stream().filter(p -> p.getProductType().name().equalsIgnoreCase("SERVICE")).count();
        dto.setTotalProducts(totalProducts);
        dto.setTotalServices(totalServices);
        // Quotations
        int totalQuotations = quotationRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId).size();
        dto.setTotalQuotations(totalQuotations);
        // Invoices
        var invoices = invoiceRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        int totalInvoices = invoices.size();
        dto.setTotalInvoices(totalInvoices);
        // Settlements
        int totalSettlements = settlementRepository.findByInvoiceCompanyCompanyIdOrderByCreatedAtDesc(companyId).size();
        dto.setTotalSettlements(totalSettlements);
        // Financials
        BigDecimal totalRevenue = invoices.stream().map(Invoice::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outstandingPayments = invoices.stream().map(Invoice::getOutstandingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscounts = invoices.stream().map(Invoice::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxCollected = invoices.stream().map(Invoice::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalRevenue(totalRevenue);
        dto.setOutstandingPayments(outstandingPayments);
        dto.setTotalDiscounts(totalDiscounts);
        dto.setTotalTaxCollected(totalTaxCollected);
        // Conversion Rate
        long convertedQuotations = invoices.stream().filter(i -> i.getQuotation() != null).count();
        dto.setQuotationToInvoiceConversionRate(totalQuotations > 0 ? (convertedQuotations * 100.0 / totalQuotations) : 0.0);
        // Invoice Status Breakdown
        dto.setInvoiceStatusBreakdown(invoices.stream().collect(Collectors.groupingBy(i -> i.getStatus().name(), Collectors.summingInt(e -> 1))));
        // Monthly Revenue Trend (last 6 months)
        dto.setMonthlyRevenueTrend(invoices.stream().collect(Collectors.groupingBy(i -> i.getInvoiceDate().getMonth().name(), Collectors.mapping(i -> i.getPaidAmount(), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))));
        // New Customers Trend (last 6 months)
        dto.setNewCustomersTrend(customerRepository.findByCompanyCompanyId(companyId).stream().collect(Collectors.groupingBy(c -> c.getCreatedAt().getMonth().name(), Collectors.summingInt(e -> 1))));
        // New Products Trend (last 6 months)
        dto.setNewProductsTrend(products.stream().collect(Collectors.groupingBy(p -> p.getCreatedAt().getMonth().name(), Collectors.summingInt(e -> 1))));
        // Top Selling Products (by quantity)
        dto.setTopSellingProducts(invoices.stream().flatMap(i -> i.getInvoiceItems().stream()).collect(Collectors.groupingBy(ii -> ii.getProduct().getProductName(), Collectors.summingInt(ii -> ii.getQuantity().intValue()))).entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(5).map(e -> e.getKey()).collect(Collectors.toList()));
        // Top Customers (by revenue)
        dto.setTopCustomers(invoices.stream().collect(Collectors.groupingBy(i -> i.getCustomer().getCustomerName(), Collectors.mapping(i -> i.getPaidAmount(), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)))).entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(5).map(e -> e.getKey()).collect(Collectors.toList()));
        // Last Invoice Date
        dto.setLastInvoiceDate(invoices.stream().map(i -> i.getInvoiceDate()).max(LocalDate::compareTo).orElse(null));
        // Last Customer Added Date
        dto.setLastCustomerAddedDate(customerRepository.findByCompanyCompanyId(companyId).stream().map(c -> c.getCreatedAt().toLocalDate()).max(LocalDate::compareTo).orElse(null));
        dto.setNotes("This report is auto-generated for company analytics.");
        return dto;
    }

    public List<ProductResponseDTO> getProductReport(Long companyId, String period) {
        List<Product> products = productRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId);
        if (products == null || products.isEmpty()) {
            throw new ReportException("No products/services found for the given company.");
        }
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(period);
        List<Invoice> invoices = invoiceRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .filter(inv -> inv.getInvoiceDate() != null && !inv.getInvoiceDate().isBefore(startDate) && !inv.getInvoiceDate().isAfter(now))
                .toList();
        List<ProductResponseDTO> productReports = new java.util.ArrayList<>();
        for (Product product : products) {
            int totalSold = 0;
            java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO;
            java.time.LocalDate lastSoldDate = null;
            for (Invoice invoice : invoices) {
                if (invoice.getInvoiceItems() != null) {
                    for (var item : invoice.getInvoiceItems()) {
                        if (item.getProduct() != null && item.getProduct().getProductId().equals(product.getProductId())) {
                            if (item.getQuantity() != null) {
                                totalSold += item.getQuantity().intValue();
                            }
                            if (item.getLineTotal() != null) {
                                totalRevenue = totalRevenue.add(item.getLineTotal());
                            }
                            if (invoice.getInvoiceDate() != null) {
                                if (lastSoldDate == null || invoice.getInvoiceDate().isAfter(lastSoldDate)) {
                                    lastSoldDate = invoice.getInvoiceDate();
                                }
                            }
                        }
                    }
                }
            }
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setHsnCode(product.getHsnCode());
            dto.setBarcode(product.getBarcode());
            dto.setUnitOfMeasure(product.getUnitOfMeasure());
            dto.setCategory(product.getCategory());
            dto.setSubcategory(product.getSubcategory());
            dto.setManufactureDate(product.getManufactureDate());
            dto.setExpiryDate(product.getExpiryDate());
            dto.setCostPrice(product.getCostPrice());
            dto.setSellingPrice(product.getSellingPrice());
            dto.setProductType(product.getProductType());
            dto.setStockQuantity(product.getStockQuantity());
            dto.setTaxRate(product.getTaxRate());
            dto.setProductImage(product.getProductImage());
            dto.setCompanyId(product.getCompany() != null ? product.getCompany().getCompanyId() : null);
            dto.setCompanyName(product.getCompany() != null ? product.getCompany().getCompanyName() : null);
            dto.setIsActive(product.getIsActive());
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());
            // Calculated fields
            dto.setProfitAmount(product.getSellingPrice().subtract(product.getCostPrice()));
            dto.setProfitMargin(product.getCostPrice().compareTo(BigDecimal.ZERO) > 0 ?
                product.getSellingPrice().subtract(product.getCostPrice()).divide(product.getCostPrice(), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
            // Reporting fields
            dto.setTotalSold(totalSold);
            dto.setTotalRevenue(totalRevenue);
            //dto.setLastSoldDate(lastSoldDate);
            productReports.add(dto);
        }
        if (productReports.isEmpty()) {
            throw new ReportException("No product/service report data found for the given period.");
        }
        return productReports;
    }

    public List<SettlementResponseDTO> getSettlementReport(Long companyId, String period) {
        List<SettlementResponseDTO> report;
        LocalDate now = LocalDate.now();
        LocalDate startDate = getStartDate(period);

        report = settlementRepository
                .findByInvoiceCompanyCompanyIdAndSettlementDateBetweenOrderBySettlementDateDesc(companyId, startDate, now)
                .stream()
                .map(settlement -> {
                    SettlementResponseDTO dto = new SettlementResponseDTO();
                    dto.setSettlementId(settlement.getSettlementId());
                    dto.setInvoiceId(settlement.getInvoice().getId());
                    dto.setInvoiceNumber(settlement.getInvoice().getInvoiceNumber());
                    dto.setSettlementDate(settlement.getSettlementDate());
                    dto.setAmountPaid(settlement.getAmountPaid());
                    dto.setPaymentMethod(settlement.getPaymentMethod());
                    dto.setPaymentMethodDisplay(settlement.getPaymentMethod() != null ? settlement.getPaymentMethod().name() : null);
                    dto.setReferenceNumber(settlement.getReferenceNumber());
                    dto.setNotes(settlement.getNotes());
                    dto.setCreatedAt(settlement.getCreatedAt());
                    // Invoice context
                    dto.setInvoiceTotalAmount(settlement.getInvoice().getTotalAmount());
                    dto.setInvoicePaidAmount(settlement.getInvoice().getPaidAmount());
                    dto.setInvoiceOutstandingAmount(settlement.getInvoice().getOutstandingAmount());
                    // Payment status
                    if (settlement.getInvoice().getOutstandingAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
                        dto.setInvoicePaymentStatus("Paid");
                    } else if (settlement.getInvoice().getPaidAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
                        dto.setInvoicePaymentStatus("Unpaid");
                    } else {
                        dto.setInvoicePaymentStatus("Partially Paid");
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        if (report == null || report.isEmpty()) {
            throw new ReportException("No settlements found for the given company and period.");
        }
        return report;
    }

    public List<QuotationResponseDTO> getQuotationReport(Long companyId, String period) {
        List<QuotationResponseDTO> report = quotationRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .filter(q -> q.getQuotationDate() != null && !q.getQuotationDate().isBefore(getStartDate(period)) && !q.getQuotationDate().isAfter(LocalDate.now()))
                .map(q -> {
                    QuotationResponseDTO dto = new QuotationResponseDTO();
                    dto.setQuotationId(q.getQuotationId());
                    dto.setQuotationNumber(q.getQuotationNumber());
                    dto.setCompanyId(q.getCompany().getCompanyId());
                    dto.setCompanyName(q.getCompany().getCompanyName());
                    dto.setCustomerId(q.getCustomer().getCustomerId());
                    dto.setCustomerName(q.getCustomer().getCustomerName());
                    dto.setCustomerEmail(q.getCustomer().getEmail());
                    dto.setQuotationDate(q.getQuotationDate());
                    dto.setSubtotal(q.getSubtotal());
                    dto.setTaxAmount(q.getTaxAmount());
                    dto.setDiscountPercentage(q.getDiscountPercentage());
                    dto.setDiscountAmount(q.getDiscountAmount());
                    dto.setTotalBeforeDiscount(q.getTotalBeforeDiscount());
                    dto.setTotalAmount(q.getTotalAmount());
                    dto.setStatus(q.getStatus());
                    dto.setDiscountReason(q.getDiscountReason());
                    dto.setNotes(q.getNotes());
                    dto.setCreatedAt(q.getCreatedAt());
                    dto.setUpdatedAt(q.getUpdatedAt());
                    dto.setItems(q.getQuotationItems() != null ? q.getQuotationItems().stream().map(item -> {
                        var itemDto = new com.xeine.dto.response.QuotationItemResponseDTO();
                        itemDto.setId(item.getId());
                        itemDto.setProductId(item.getProduct().getProductId());
                        itemDto.setProductName(item.getProduct().getProductName());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        itemDto.setTaxRate(item.getTaxRate());
                        itemDto.setTaxAmount(item.getTaxAmount());
                        itemDto.setLineTotal(item.getLineTotal());
                        itemDto.setDescription(item.getDescription());
                        return itemDto;
                    }).collect(Collectors.toList()) : null);
                    // Calculated fields
                    dto.setTotalItems(q.getQuotationItems() != null ? q.getQuotationItems().size() : 0);
                    // Check if invoice exists for this quotation
                    var invoiceOpt = invoiceRepository.findByQuotationQuotationIdAndCompanyCompanyId(q.getQuotationId(), companyId);
                    dto.setHasInvoice(invoiceOpt.isPresent());
                    dto.setInvoiceNumber(invoiceOpt.map(Invoice::getInvoiceNumber).orElse(null));
                    return dto;
                })
                .collect(Collectors.toList());
        if (report == null || report.isEmpty()) {
            throw new ReportException("No quotations found for the given company or period.");
        }
        return report;
    }

    private LocalDate getStartDate(String period) {
        LocalDate now = LocalDate.now();
        if (period == null || period.isBlank()) {
            return now.minusWeeks(1); // default to week if missing
        }
        switch (period.toLowerCase()) {
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "quarter":
                return now.minusMonths(3);
            default:
                throw new ReportException("Invalid period value: " + period + ". Allowed values: week, month, quarter");
        }
    }
}
