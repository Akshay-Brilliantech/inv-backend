package com.xeine.services;


import com.xeine.dto.request.QuotationCreateRequest;
import com.xeine.dto.request.QuotationItemRequest;
import com.xeine.dto.request.QuotationUpdateRequest;
import com.xeine.dto.response.QuotationItemResponseDTO;
import com.xeine.dto.response.QuotationResponseDTO;
import com.xeine.enums.QuotationStatus;
import com.xeine.exception.*;
import com.xeine.models.*;
import com.xeine.repository.*;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private  QuotationItemRepository quotationItemRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;


    @Transactional
    public QuotationResponseDTO createQuotation(QuotationCreateRequest request) {
        log.info("Creating quotation for company: {} and customer: {}", request.getCompanyId(), request.getCustomerId());

        // Validate company and customer
        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Active company not found with ID: " + request.getCompanyId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        // Validate customer belongs to company
        if (!customer.getCompany().getCompanyId().equals(request.getCompanyId())) {
            throw new BusinessValidationException("Customer does not belong to the specified company");
        }

        // Create quotation
        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(generateQuotationNumber(company.getCompanyId()));
        quotation.setCompany(company);
        quotation.setCustomer(customer);
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setDiscountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO);
        quotation.setDiscountReason(request.getDiscountReason());
        quotation.setNotes(request.getNotes());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCreatedAt(LocalDateTime.now());
        quotation.setUpdatedAt(LocalDateTime.now());

        processQuotationItems(quotation, request.getItems());


        calculateQuotationTotals(quotation);

        Quotation savedQuotation = quotationRepository.save(quotation);
        log.info("Successfully created quotation with ID: {} and number: {}", savedQuotation.getQuotationId(), savedQuotation.getQuotationNumber());

        return convertToResponseDTO(savedQuotation);
    }


    @Transactional
    public QuotationResponseDTO updateQuotation(Long quotationId, Long companyId, QuotationUpdateRequest request) {
        log.info("Updating quotation ID: {} for company: {}", quotationId, companyId);

        Quotation existingQuotation = quotationRepository.findByIdAndCompany(quotationId, companyId)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found with ID: " + quotationId));


        if (!QuotationStatus.DRAFT.equals(request.getStatus())) {
            throw new RuntimeException("Quotation update request failed");
        }

        // Validate and update customer
        if (!existingQuotation.getCustomer().getCustomerId().equals(request.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + request.getCustomerId()));

            if (!newCustomer.getCompany().getCompanyId().equals(companyId)) {
                throw new BusinessValidationException("Customer does not belong to the specified company");
            }
            existingQuotation.setCustomer(newCustomer);
        }

        // Update fields
        updateQuotationFields(existingQuotation, request);

        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Remove existing items
            quotationItemRepository.deleteByQuotationQuotationId(quotationId);
            existingQuotation.getQuotationItems().clear();

            // Add new items
            processQuotationItems(existingQuotation, request.getItems());
        }

        // Recalculate totals
        calculateQuotationTotals(existingQuotation);

        existingQuotation.setUpdatedAt(LocalDateTime.now());
        Quotation updatedQuotation = quotationRepository.save(existingQuotation);

        log.info("Successfully updated quotation with ID: {}", quotationId);
        return convertToResponseDTO(updatedQuotation);
    }

    private void updateQuotationFields(Quotation existingQuotation, QuotationUpdateRequest request) {
        log.debug("Updating quotation fields for quotation ID: {}", existingQuotation.getQuotationId());

        // Update quotation date if provided
        if (request.getQuotationDate() != null) {
            existingQuotation.setQuotationDate(request.getQuotationDate());
        }

        // Update discount percentage (can be null to remove discount)
        if (request.getDiscountPercentage() != null) {
            existingQuotation.setDiscountPercentage(request.getDiscountPercentage());
        } else {
            existingQuotation.setDiscountPercentage(BigDecimal.ZERO);
        }


        existingQuotation.setDiscountReason(request.getDiscountReason());


        existingQuotation.setNotes(request.getNotes());


        if (request.getStatus() != null) {
            existingQuotation.setStatus(request.getStatus());
        }

        log.debug("Successfully updated quotation fields for quotation ID: {}", existingQuotation.getQuotationId());
    }


    public List<QuotationResponseDTO> getAllQuotationsByCompany(Long companyId) {
        List<Quotation> quotations = quotationRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        return quotations.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get quotations by status
     */
    public List<QuotationResponseDTO> getQuotationsByStatus(Long companyId, QuotationStatus status) {
        List<Quotation> quotations = quotationRepository.findByStatusAndCompanyCompanyIdOrderByCreatedAtDesc(status, companyId);
        return quotations.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get quotations by customer
     */
    public List<QuotationResponseDTO> getQuotationsByCustomer(Long customerId, @Min(1) Long companyId) {
        List<Quotation> quotations = quotationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return quotations.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuotation(Long quotationId, Long companyId) {
        log.info("Soft deleting quotation ID: {} for company: {}", quotationId, companyId);

        // Verify quotation exists and belongs to company
        Quotation quotation = quotationRepository.findByIdAndCompany(quotationId, companyId)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found with ID: " + quotationId));



        // Perform soft delete
        int deletedCount = quotationRepository.softDeleteByIdAndCompany(quotationId, companyId);

        if (deletedCount == 0) {
            throw new BusinessValidationException("Failed to delete quotation with ID: " + quotationId);
        }

        log.info("Successfully soft deleted quotation with ID: {} and number: {}",
                quotationId, quotation.getQuotationNumber());
    }


    private QuotationResponseDTO convertToResponseDTO(Quotation quotation) {
        QuotationResponseDTO dto = new QuotationResponseDTO();
        dto.setQuotationId(quotation.getQuotationId());
        dto.setQuotationNumber(quotation.getQuotationNumber());
        dto.setCompanyId(quotation.getCompany().getCompanyId());
        dto.setCompanyName(quotation.getCompany().getCompanyName());
        dto.setCustomerId(quotation.getCustomer().getCustomerId());
        dto.setCustomerName(quotation.getCustomer().getCustomerName());
        dto.setQuotationDate(quotation.getQuotationDate());
        dto.setSubtotal(quotation.getSubtotal());
        dto.setTaxAmount(quotation.getTaxAmount());
        dto.setDiscountPercentage(quotation.getDiscountPercentage());
        dto.setDiscountAmount(quotation.getDiscountAmount());
        dto.setTotalBeforeDiscount(quotation.getTotalBeforeDiscount());
        dto.setTotalAmount(quotation.getTotalAmount());
        dto.setStatus(quotation.getStatus());
        dto.setDiscountReason(quotation.getDiscountReason());
        dto.setNotes(quotation.getNotes());
        dto.setCreatedAt(quotation.getCreatedAt());
        dto.setUpdatedAt(quotation.getUpdatedAt());


        List<QuotationItemResponseDTO> itemDTOs = quotation.getQuotationItems().stream().map(item -> {
            QuotationItemResponseDTO itemDTO = new QuotationItemResponseDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getProductId());
            itemDTO.setProductName(item.getProduct().getProductName());
            itemDTO.setProductType(item.getProduct().getProductType());
            itemDTO.setProductImage(item.getProduct().getProductImage());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setUnitPrice(item.getUnitPrice());
            itemDTO.setTaxRate(item.getTaxRate());
            itemDTO.setTaxAmount(item.getTaxAmount());
            itemDTO.setLineTotal(item.getLineTotal());
            itemDTO.setDescription(item.getDescription());
            return itemDTO;
        }).toList();

        dto.setItems(itemDTOs);

        return dto;
    }

    public static String generateQuotationNumber(Long id) {
        return "QUOT-" + System.currentTimeMillis() + "-" + id;
    }

    private void processQuotationItems(Quotation quotation, List<QuotationItemRequest> itemRequests) {
        for (QuotationItemRequest itemRequest : itemRequests) {
            // Validate product exists and belongs to company
            Product product = productRepository.findActiveByIdAndCompany(
                            itemRequest.getProductId(), quotation.getCompany().getCompanyId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId()));


            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setTaxRate(itemRequest.getTaxRate());
            item.setDescription(itemRequest.getDescription());

            // Calculate amounts
            BigDecimal lineSubtotal = itemRequest.getQuantity().multiply(itemRequest.getUnitPrice());
            BigDecimal lineTaxAmount = lineSubtotal.multiply(itemRequest.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = lineSubtotal.add(lineTaxAmount);

            item.setTaxAmount(lineTaxAmount);
            item.setLineTotal(lineTotal);

            quotation.getQuotationItems().add(item);
        }

    }

    private void calculateQuotationTotals(Quotation quotation) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // Sum up all line items
        for (QuotationItem item : quotation.getQuotationItems()) {
            BigDecimal itemSubtotal = item.getQuantity().multiply(item.getUnitPrice());
            subtotal = subtotal.add(itemSubtotal);
            totalTax = totalTax.add(item.getTaxAmount());
        }

        quotation.setSubtotal(subtotal);
        quotation.setTaxAmount(totalTax);

        // Calculate total before discount
        BigDecimal totalBeforeDiscount = subtotal.add(totalTax);
        quotation.setTotalBeforeDiscount(totalBeforeDiscount);

        // Calculate discount amount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (quotation.getDiscountPercentage() != null &&
                quotation.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = totalBeforeDiscount
                    .multiply(quotation.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        quotation.setDiscountAmount(discountAmount);


        BigDecimal totalAmount = totalBeforeDiscount.subtract(discountAmount);
        quotation.setTotalAmount(totalAmount);
    }

    public QuotationResponseDTO getQuotationById(Long quotationId, Long companyId) {
        log.info("Retrieving quotation ID: {} for company: {}", quotationId, companyId);

        Quotation quotation = quotationRepository.findByIdAndCompany(quotationId, companyId)
                .orElseThrow(() -> new QuotationNotFoundException("Quotation not found with ID: " + quotationId));

        log.info("Successfully retrieved quotation with ID: {} and number: {}",
                quotation.getQuotationId(), quotation.getQuotationNumber());

        return convertToResponseDTO(quotation);
    }

}
