package com.xeine.services;


import com.xeine.dto.request.SettlementCreateRequest;
import com.xeine.dto.response.SettlementResponseDTO;
import com.xeine.enums.InvoiceStatus;
import com.xeine.enums.PaymentMethod;
import com.xeine.exception.*;
import com.xeine.models.Invoice;
import com.xeine.models.Settlement;
import com.xeine.repository.InvoiceRepository;
import com.xeine.repository.SettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * Create a settlement (payment) for an invoice
     */
    @Transactional
    public SettlementResponseDTO createSettlement(SettlementCreateRequest request) {
        log.info("Creating settlement for invoice {} with amount {}",
                request.getInvoiceId(), request.getAmountPaid());

        // 1. Validate and get invoice
        Invoice invoice = invoiceRepository.findActiveByIdAndCompany(
                        request.getInvoiceId(), request.getCompanyId())
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found with ID: " + request.getInvoiceId()));

        // 2. Validate payment amount
        validatePaymentAmount(invoice, request.getAmountPaid());

        // 3. Create settlement
        Settlement settlement = createSettlementEntity(request, invoice);

        // 4. Update invoice payment amounts
        updateInvoicePaymentAmounts(invoice, request.getAmountPaid());

        // 5. Update invoice status based on payment
        updateInvoiceStatus(invoice);

        // 6. Save settlement and updated invoice
        Settlement savedSettlement = settlementRepository.save(settlement);
        invoiceRepository.save(invoice);

        log.info("Successfully created settlement {} for invoice {} with amount {}",
                savedSettlement.getSettlementId(), invoice.getInvoiceNumber(), request.getAmountPaid());

        return convertToResponseDTO(savedSettlement);
    }


    public SettlementResponseDTO getSettlementById(Long settlementId, Long companyId) {
        Settlement settlement = settlementRepository.findBySettlementIdAndCompany(settlementId, companyId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found with ID: " + settlementId));

        return convertToResponseDTO(settlement);
    }

    /**
     * Get all settlements for a company
     */
    public List<SettlementResponseDTO> getSettlementsByCompany(Long companyId) {
        List<Settlement> settlements = settlementRepository.findByInvoiceCompanyCompanyIdOrderByCreatedAtDesc(companyId);

        return settlements.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get settlements by date range
     */
    public List<SettlementResponseDTO> getSettlementsByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<Settlement> settlements = settlementRepository.findByInvoiceCompanyCompanyIdAndSettlementDateBetweenOrderBySettlementDateDesc(
                companyId, startDate, endDate);

        return settlements.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get settlements by payment method
     */
    public List<SettlementResponseDTO> getSettlementsByPaymentMethod(Long companyId, PaymentMethod paymentMethod) {
        List<Settlement> settlements = settlementRepository.findByInvoiceCompanyCompanyIdAndPaymentMethodOrderByCreatedAtDesc(
                companyId, paymentMethod);

        return settlements.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get total settlements amount for a company
     */
    public BigDecimal getTotalSettlementsAmount(Long companyId) {
        BigDecimal total = settlementRepository.getTotalSettlementsAmountByCompany(companyId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Get total settlements amount by payment method
     */
    public BigDecimal getTotalSettlementsAmountByPaymentMethod(Long companyId, PaymentMethod paymentMethod) {
        BigDecimal total = settlementRepository.getTotalSettlementsAmountByCompanyAndPaymentMethod(companyId, paymentMethod);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ===== PRIVATE HELPER METHODS =====

    /**
     * Validate payment amount
     */
    private void validatePaymentAmount(Invoice invoice, BigDecimal amountPaid) {
        // Check if amount is positive
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Payment amount must be greater than zero");
        }

        // Check if amount doesn't exceed outstanding amount
        if (amountPaid.compareTo(invoice.getOutstandingAmount()) > 0) {
            throw new BusinessValidationException(
                    String.format("Payment amount %.2f exceeds outstanding amount %.2f",
                            amountPaid, invoice.getOutstandingAmount()));
        }

        // Check if invoice is already fully paid
        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessValidationException("Invoice is already fully paid");
        }
    }

    /**
     * Create settlement entity
     */
    private Settlement createSettlementEntity(SettlementCreateRequest request, Invoice invoice) {
        Settlement settlement = new Settlement();

        settlement.setInvoice(invoice);
        settlement.setSettlementDate(request.getSettlementDate() != null ?
                request.getSettlementDate() : LocalDate.now());
        settlement.setAmountPaid(request.getAmountPaid());
        settlement.setPaymentMethod(request.getPaymentMethod() != null ?
                request.getPaymentMethod() : PaymentMethod.getDefault());
        settlement.setReferenceNumber(request.getReferenceNumber());
        settlement.setNotes(request.getNotes());
        settlement.setCreatedAt(LocalDateTime.now());

        return settlement;
    }

    /**
     * Update invoice payment amounts
     */
    private void updateInvoicePaymentAmounts(Invoice invoice, BigDecimal paymentAmount) {
        // Update paid amount
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(paymentAmount);
        invoice.setPaidAmount(newPaidAmount);

        // Update outstanding amount
        BigDecimal newOutstandingAmount = invoice.getTotalAmount().subtract(newPaidAmount);
        invoice.setOutstandingAmount(newOutstandingAmount);
    }

    /**
     * Update invoice status based on payment
     */
    private void updateInvoiceStatus(Invoice invoice) {
        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            // Fully paid
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Partially paid - keep as PENDING unless it was OVERDUE
            if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
                invoice.setStatus(InvoiceStatus.PENDING);
            }
        }
    }

    /**
     * Convert Settlement to Response DTO
     */
    private SettlementResponseDTO convertToResponseDTO(Settlement settlement) {
        SettlementResponseDTO dto = new SettlementResponseDTO();

        dto.setSettlementId(settlement.getSettlementId());
        dto.setInvoiceId(settlement.getInvoice().getId());
        dto.setInvoiceNumber(settlement.getInvoice().getInvoiceNumber());
        dto.setSettlementDate(settlement.getSettlementDate());
        dto.setAmountPaid(settlement.getAmountPaid());
        dto.setPaymentMethod(settlement.getPaymentMethod());
        dto.setPaymentMethodDisplay(settlement.getPaymentMethod().getDisplayName());
        dto.setReferenceNumber(settlement.getReferenceNumber());
        dto.setNotes(settlement.getNotes());
        dto.setCreatedAt(settlement.getCreatedAt());

        // Invoice details for context
        Invoice invoice = settlement.getInvoice();
        dto.setInvoiceTotalAmount(invoice.getTotalAmount());
        dto.setInvoicePaidAmount(invoice.getPaidAmount());
        dto.setInvoiceOutstandingAmount(invoice.getOutstandingAmount());
        dto.setInvoicePaymentStatus(determinePaymentStatus(invoice));

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
}
