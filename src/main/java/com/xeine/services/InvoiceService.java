package com.xeine.services;

import com.xeine.dto.request.ConvertQuotationToInvoiceRequest;
import com.xeine.dto.request.InvoiceCreateRequest;
import com.xeine.dto.request.InvoiceItemCreateRequest;
import com.xeine.dto.response.InvoiceResponseDTO;
import com.xeine.dto.response.InvoiceItemResponseDTO;
import com.xeine.enums.InvoiceStatus;
import com.xeine.enums.QuotationStatus;
import com.xeine.exception.*;
import com.xeine.models.*;
import com.xeine.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create invoice directly (without quotation) - WITH INVENTORY MANAGEMENT
     */
    @Transactional
    public InvoiceResponseDTO createInvoiceDirectly(InvoiceCreateRequest request) {
        log.info("Creating invoice directly for company {} and customer {}",
                request.getCompanyId(), request.getCustomerId());

        // 1. Validate company
        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with ID: " + request.getCompanyId()));

        // 2. Validate customer belongs to company
        Customer customer = customerRepository.findByCustomerIdAndCompanyCompanyId(request.getCustomerId(), request.getCompanyId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        // 3. Validate and get products for all items
        List<Product> products = validateAndGetProducts(request.getItems(), request.getCompanyId());

        // 4. NEW: Check inventory availability BEFORE creating invoice
        validateInventoryAvailability(request.getItems(), products);

        // 5. Create invoice
        Invoice invoice = createDirectInvoice(request, company, customer);

        // 6. Create invoice items and calculate totals
        List<InvoiceItem> invoiceItems = createInvoiceItemsFromRequest(request.getItems(), invoice, products);
        invoice.setInvoiceItems(invoiceItems);

        // 7. Calculate and set financial totals
        calculateInvoiceTotals(invoice, request.getDiscountPercentage());

        // 8. Save invoice first
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 9. NEW: Update inventory after successful invoice creation
        updateInventoryForInvoiceItems(request.getItems(), products);

        log.info("Successfully created invoice {} directly with inventory updated", savedInvoice.getInvoiceNumber());

        return convertToResponseDTO(savedInvoice);
    }

    @Transactional
    public InvoiceResponseDTO convertQuotationToInvoice(ConvertQuotationToInvoiceRequest request) {
        log.info("Converting quotation {} to invoice for company {}",
                request.getQuotationId(), request.getCompanyId());

        Quotation quotation = quotationRepository.findActiveByIdAndCompany(
                        request.getQuotationId(), request.getCompanyId())
                .orElseThrow(() -> new QuotationNotFoundException(
                        "Quotation not found with ID: " + request.getQuotationId()));

        // 2. Validate quotation status
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new BusinessValidationException(
                    "Only DRAFT quotations can be converted to invoice. Current status: " + quotation.getStatus());
        }

        // 3. Check if quotation is already converted
        if (quotation.getInvoice() != null) {
            throw new BusinessValidationException(
                    "Quotation is already converted to invoice: " + quotation.getInvoice().getInvoiceNumber());
        }

        // 4. Validate quotation has items
        if (quotation.getQuotationItems() == null || quotation.getQuotationItems().isEmpty()) {
            throw new BusinessValidationException(
                    "Cannot convert quotation with no items to invoice");
        }

        // 5. NEW: Check inventory availability for quotation items
        validateInventoryAvailabilityForQuotation(quotation.getQuotationItems());

        // 6. Create invoice from quotation
        Invoice invoice = createInvoiceFromQuotation(quotation, request);

        // 7. Create invoice items from quotation items
        List<InvoiceItem> invoiceItems = createInvoiceItemsFromQuotationItems(
                quotation.getQuotationItems(), invoice);
        invoice.setInvoiceItems(invoiceItems);

        quotation.setStatus(QuotationStatus.CONVERTED);
        quotation.setUpdatedAt(LocalDateTime.now());

        // 8. Save invoice first
        Invoice savedInvoice = invoiceRepository.save(invoice);
        quotationRepository.save(quotation);

        // 9. NEW: Update inventory after successful invoice creation
        updateInventoryForQuotationItems(quotation.getQuotationItems());

        log.info("Successfully converted quotation {} to invoice {} with inventory updated",
                quotation.getQuotationNumber(), savedInvoice.getInvoiceNumber());

        return convertToResponseDTO(savedInvoice);
    }



    /**
     * Validate inventory availability for direct invoice items
     */
    private void validateInventoryAvailability(List<InvoiceItemCreateRequest> itemRequests, List<Product> products) {
        List<String> outOfStockItems = new ArrayList<>();

        for (int i = 0; i < itemRequests.size(); i++) {
            InvoiceItemCreateRequest itemRequest = itemRequests.get(i);
            Product product = products.get(i);

            int requestedQuantity = itemRequest.getQuantity().intValue();

            if (!product.hasSufficientStock(requestedQuantity)) {
                int availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                outOfStockItems.add(String.format(

                        product.getProductName(),
                        requestedQuantity,
                        availableStock
                ));
            }
        }

        if (!outOfStockItems.isEmpty()) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for the following items: " + String.join("; ", outOfStockItems)
            );
        }
    }

    /**
     * Validate inventory availability for quotation items
     */
    private void validateInventoryAvailabilityForQuotation(List<QuotationItem> quotationItems) {
        List<String> outOfStockItems = new ArrayList<>();

        for (QuotationItem quotationItem : quotationItems) {
            Product product = quotationItem.getProduct();
            int requestedQuantity = quotationItem.getQuantity().intValue();

            if (!product.hasSufficientStock(requestedQuantity)) {
                int availableStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                outOfStockItems.add(String.format(
                        "Product '%s' (Code: %s) - Requested: %d, Available: %d",
                        product.getProductName(),
                        requestedQuantity,
                        availableStock
                ));
            }
        }

        if (!outOfStockItems.isEmpty()) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for the following items: " + String.join("; ", outOfStockItems)
            );
        }
    }

    /**
     * Update inventory for direct invoice items
     */
    private void updateInventoryForInvoiceItems(List<InvoiceItemCreateRequest> itemRequests, List<Product> products) {
        List<Product> productsToUpdate = new ArrayList<>();

        for (int i = 0; i < itemRequests.size(); i++) {
            InvoiceItemCreateRequest itemRequest = itemRequests.get(i);
            Product product = products.get(i);

            int quantityToReduce = itemRequest.getQuantity().intValue();
            int previousStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            // Use Product's helper method to reduce stock
            product.reduceStock(quantityToReduce);
            productsToUpdate.add(product);



        }

        // Batch save all updated products
        productRepository.saveAll(productsToUpdate);
    }

    /**
     * Update inventory for quotation items
     */
    private void updateInventoryForQuotationItems(List<QuotationItem> quotationItems) {
        List<Product> productsToUpdate = new ArrayList<>();

        for (QuotationItem quotationItem : quotationItems) {
            Product product = quotationItem.getProduct();

            int quantityToReduce = quotationItem.getQuantity().intValue();
            int previousStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            // Use Product's helper method to reduce stock
            product.reduceStock(quantityToReduce);
            productsToUpdate.add(product);

            log.info("Updated inventory for product {} ({}): {} -> {} (reduced by {})",
                    product.getProductName(),

                    previousStock,
                    product.getStockQuantity(),
                    quantityToReduce);


        }

        // Batch save all updated products
        productRepository.saveAll(productsToUpdate);
    }






    /**
     * Create invoice entity for direct creation
     */
    private Invoice createDirectInvoice(InvoiceCreateRequest request, Company company, Customer customer) {
        Invoice invoice = new Invoice();

        // Generate unique invoice number
        invoice.setInvoiceNumber("INV" + UUID.randomUUID());

        // Set basic information
        invoice.setCompany(company);
        invoice.setCustomer(customer);
        invoice.setQuotation(null);

        // Set dates
        invoice.setInvoiceDate(request.getInvoiceDate() != null ?
                request.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ?
                request.getDueDate() : LocalDate.now().plusDays(30));

        // Initialize financial fields (will be calculated later)
        invoice.setSubtotal(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountPercentage(request.getDiscountPercentage());
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalBeforeDiscount(BigDecimal.ZERO);
        invoice.setTotalAmount(BigDecimal.ZERO);

        // Initialize payment information
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(BigDecimal.ZERO);

        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setDiscountReason(request.getDiscountReason());
        invoice.setNotes(request.getNotes());

        // Set timestamp
        invoice.setCreatedAt(LocalDateTime.now());

        return invoice;
    }

    private List<Product> validateAndGetProducts(List<InvoiceItemCreateRequest> itemRequests, Long companyId) {
        List<Product> products = new ArrayList<>();

        for (InvoiceItemCreateRequest itemRequest : itemRequests) {
            Product product = productRepository.findActiveByIdAndCompany(itemRequest.getProductId(), companyId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Product not found with ID: " + itemRequest.getProductId()));
            products.add(product);
        }

        return products;
    }

    private List<InvoiceItem> createInvoiceItemsFromRequest(
            List<InvoiceItemCreateRequest> itemRequests, Invoice invoice, List<Product> products) {

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (int i = 0; i < itemRequests.size(); i++) {
            InvoiceItemCreateRequest itemRequest = itemRequests.get(i);
            Product product = products.get(i);

            InvoiceItem invoiceItem = new InvoiceItem();

            // Link to invoice and product
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(product);

            // Set quantity and unit price
            invoiceItem.setQuantity(itemRequest.getQuantity());
            invoiceItem.setUnitPrice(product.getSellingPrice()); // Use product's selling price

            // Set tax rate from product
            invoiceItem.setTaxRate(product.getTaxRate() != null ? product.getTaxRate() : BigDecimal.ZERO);

            // Calculate line total before tax
            BigDecimal lineSubtotal = itemRequest.getQuantity().multiply(product.getSellingPrice());

            // Calculate tax amount
            BigDecimal taxAmount = lineSubtotal.multiply(invoiceItem.getTaxRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            invoiceItem.setTaxAmount(taxAmount);

            // Calculate line total (including tax)
            BigDecimal lineTotal = lineSubtotal.add(taxAmount);
            invoiceItem.setLineTotal(lineTotal);

            // Set description
            invoiceItem.setDescription(itemRequest.getDescription() != null ?
                    itemRequest.getDescription() : product.getProductName());

            invoiceItems.add(invoiceItem);
        }

        return invoiceItems;
    }

    private void calculateInvoiceTotals(Invoice invoice, BigDecimal discountPercentage) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            BigDecimal lineSubtotal = item.getQuantity().multiply(item.getUnitPrice());
            subtotal = subtotal.add(lineSubtotal);
            totalTax = totalTax.add(item.getTaxAmount());
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(totalTax);

        // Calculate total before discount
        BigDecimal totalBeforeDiscount = subtotal.add(totalTax);
        invoice.setTotalBeforeDiscount(totalBeforeDiscount);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = totalBeforeDiscount.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        invoice.setDiscountAmount(discountAmount);

        BigDecimal totalAmount = totalBeforeDiscount.subtract(discountAmount);
        invoice.setTotalAmount(totalAmount);

        invoice.setOutstandingAmount(totalAmount);
    }

    private Invoice createInvoiceFromQuotation(Quotation quotation, ConvertQuotationToInvoiceRequest request) {
        Invoice invoice = new Invoice();

        // Generate unique invoice number
        invoice.setInvoiceNumber("INV" + UUID.randomUUID());

        // Copy basic information from quotation
        invoice.setCompany(quotation.getCompany());
        invoice.setCustomer(quotation.getCustomer());
        invoice.setQuotation(quotation);

        invoice.setInvoiceDate(request.getInvoiceDate() != null ?
                request.getInvoiceDate() : LocalDate.now());
        invoice.setDueDate(request.getDueDate() != null ?
                request.getDueDate() : LocalDate.now().plusDays(30));

        invoice.setSubtotal(quotation.getSubtotal());
        invoice.setTaxAmount(quotation.getTaxAmount());
        invoice.setDiscountPercentage(quotation.getDiscountPercentage());
        invoice.setDiscountAmount(quotation.getDiscountAmount());
        invoice.setTotalBeforeDiscount(quotation.getTotalBeforeDiscount());
        invoice.setTotalAmount(quotation.getTotalAmount());

        // Initialize payment information
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(quotation.getTotalAmount());

        // Set status and notes
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setDiscountReason(quotation.getDiscountReason());
        invoice.setNotes(request.getNotes() != null ? request.getNotes() : quotation.getNotes());

        // Set timestamp
        invoice.setCreatedAt(LocalDateTime.now());

        return invoice;
    }

    private List<InvoiceItem> createInvoiceItemsFromQuotationItems(
            List<QuotationItem> quotationItems, Invoice invoice) {

        List<InvoiceItem> invoiceItems = new ArrayList<>();

        for (QuotationItem quotationItem : quotationItems) {
            InvoiceItem invoiceItem = new InvoiceItem();

            // Link to invoice and product
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(quotationItem.getProduct());

            // Copy item details
            invoiceItem.setQuantity(quotationItem.getQuantity());
            invoiceItem.setUnitPrice(quotationItem.getUnitPrice());
            invoiceItem.setTaxRate(quotationItem.getTaxRate());
            invoiceItem.setTaxAmount(quotationItem.getTaxAmount());
            invoiceItem.setLineTotal(quotationItem.getLineTotal());
            invoiceItem.setDescription(quotationItem.getDescription());

            invoiceItems.add(invoiceItem);
        }

        return invoiceItems;
    }

    public InvoiceResponseDTO getInvoiceById(Long invoiceId, Long companyId) {
        Invoice invoice = invoiceRepository.findActiveByIdAndCompany(invoiceId, companyId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        return convertToResponseDTO(invoice);
    }

    public List<InvoiceResponseDTO> getAllInvoicesByCompany(Long companyId) {
        List<Invoice> invoices = invoiceRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        return invoices.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDTO getInvoiceByQuotationId(Long quotationId, Long companyId) {
        Invoice invoice = invoiceRepository.findByQuotationQuotationIdAndCompanyCompanyId(quotationId, companyId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "No invoice found for quotation ID: " + quotationId));

        return convertToResponseDTO(invoice);
    }

    public boolean canConvertToInvoice(Long quotationId, Long companyId) {
        try {
            Quotation quotation = quotationRepository.findActiveByIdAndCompany(quotationId, companyId)
                    .orElse(null);

            if (quotation == null) {
                return false;
            }

            return quotation.getStatus() == QuotationStatus.DRAFT && quotation.getInvoice() == null;

        } catch (Exception e) {
            return false;
        }
    }

    private InvoiceResponseDTO convertToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();

        dto.setInvoiceId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());

        dto.setCompanyId(invoice.getCompany().getCompanyId());
        dto.setCompanyName(invoice.getCompany().getCompanyName());
        dto.setCustomerId(invoice.getCustomer().getCustomerId());
        dto.setCustomerName(invoice.getCustomer().getCustomerName());

        if (invoice.getQuotation() != null) {
            dto.setQuotationId(invoice.getQuotation().getQuotationId());
            dto.setQuotationNumber(invoice.getQuotation().getQuotationNumber());
        }

        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountPercentage(invoice.getDiscountPercentage());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalBeforeDiscount(invoice.getTotalBeforeDiscount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setOutstandingAmount(invoice.getOutstandingAmount());

        // Status and notes
        dto.setStatus(invoice.getStatus());
        dto.setDiscountReason(invoice.getDiscountReason());
        dto.setNotes(invoice.getNotes());
        dto.setCreatedAt(invoice.getCreatedAt());

        // Calculate derived fields
        dto.setPaymentStatus(determinePaymentStatus(invoice));
        dto.setIsOverdue(isInvoiceOverdue(invoice));
        dto.setDaysOverdue(calculateDaysOverdue(invoice));

        dto.setInvoiceItems(convertInvoiceItemsToDTO(invoice.getInvoiceItems()));

        return dto;
    }

    private String determinePaymentStatus(Invoice invoice) {
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
            return "Unpaid";
        } else if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) >= 0) {
            return "Paid";
        } else {
            return "Partially Paid";
        }
    }

    private boolean isInvoiceOverdue(Invoice invoice) {
        return invoice.getDueDate() != null &&
                invoice.getDueDate().isBefore(LocalDate.now()) &&
                invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private Integer calculateDaysOverdue(Invoice invoice) {
        if (!isInvoiceOverdue(invoice)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
    }

    private List<InvoiceItemResponseDTO> convertInvoiceItemsToDTO(List<InvoiceItem> invoiceItems) {
        return invoiceItems.stream()
                .map(this::convertInvoiceItemToDTO)
                .collect(Collectors.toList());
    }

    private InvoiceItemResponseDTO convertInvoiceItemToDTO(InvoiceItem item) {
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
}