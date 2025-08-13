package com.xeine.controllers;

import com.xeine.dto.request.ConvertQuotationToInvoiceRequest;
import com.xeine.dto.request.InvoiceCreateRequest;
import com.xeine.dto.response.InvoiceResponseDTO;
import com.xeine.exception.InsufficientInventoryException;
import com.xeine.models.Product;
import com.xeine.services.InvoiceService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@Slf4j
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> createInvoiceDirectly(
            @Valid @RequestBody InvoiceCreateRequest request) {
        try {
            log.info("Creating invoice directly for company {} and customer {}",
                    request.getCompanyId(), request.getCustomerId());

            InvoiceResponseDTO invoice = invoiceService.createInvoiceDirectly(request);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    true,
                    HttpStatus.CREATED.value(),
                    "Invoice created successfully with inventory updated",
                    invoice
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InsufficientInventoryException e) {
            log.warn("Insufficient inventory for invoice creation: {}", e.getMessage());
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "Insufficient inventory: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Error creating invoice directly: {}", e.getMessage(), e);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to create invoice: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/convert-from-quotation")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> convertQuotationToInvoice(
            @Valid @RequestBody ConvertQuotationToInvoiceRequest request) {
        try {
            log.info("Converting quotation {} to invoice for company {}",
                    request.getQuotationId(), request.getCompanyId());

            InvoiceResponseDTO invoice = invoiceService.convertQuotationToInvoice(request);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    true,
                    HttpStatus.CREATED.value(),
                    "Quotation converted to invoice successfully with inventory updated",
                    invoice
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InsufficientInventoryException e) {
            log.warn("Insufficient inventory for quotation conversion: {}", e.getMessage());
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "Insufficient inventory: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("Error converting quotation to invoice: {}", e.getMessage(), e);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to convert quotation: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/can-convert/{quotationId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canConvertQuotation(
            @PathVariable Long quotationId,
            @RequestParam Long companyId) {

        boolean canConvert = invoiceService.canConvertToInvoice(quotationId, companyId);
        Map<String, Boolean> result = Map.of("canConvert", canConvert);

        ApiResponse<Map<String, Boolean>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                canConvert ? "Quotation can be converted" : "Quotation cannot be converted",
                result
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoice(
            @PathVariable Long invoiceId,
            @RequestParam Long companyId) {
        try {
            InvoiceResponseDTO invoice = invoiceService.getInvoiceById(invoiceId, companyId);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Invoice retrieved successfully",
                    invoice
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving invoice {}: {}", invoiceId, e.getMessage());
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.NOT_FOUND.value(),
                    "Invoice not found: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getAllInvoicesByCompany(
            @PathVariable Long companyId) {
        try {
            List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoicesByCompany(companyId);
            ApiResponse<List<InvoiceResponseDTO>> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Invoices retrieved successfully",
                    invoices
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving invoices for company {}: {}", companyId, e.getMessage());
            ApiResponse<List<InvoiceResponseDTO>> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve invoices: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/quotation/{quotationId}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoiceByQuotation(
            @PathVariable Long quotationId,
            @RequestParam Long companyId) {
        try {
            InvoiceResponseDTO invoice = invoiceService.getInvoiceByQuotationId(quotationId, companyId);
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Invoice retrieved successfully",
                    invoice
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving invoice for quotation {}: {}", quotationId, e.getMessage());
            ApiResponse<InvoiceResponseDTO> response = new ApiResponse<>(
                    false,
                    HttpStatus.NOT_FOUND.value(),
                    "Invoice not found for quotation: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }



}