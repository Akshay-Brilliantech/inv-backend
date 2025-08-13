package com.xeine.controllers;

import com.xeine.dto.request.QuotationCreateRequest;
import com.xeine.dto.request.QuotationUpdateRequest;
import com.xeine.dto.response.QuotationResponseDTO;
import com.xeine.enums.QuotationStatus;
import com.xeine.services.QuotationService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@Validated
public class QuotationController {

    @Autowired
    private QuotationService quotationService;


    @PostMapping
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> createQuotation(
            @Valid @RequestBody QuotationCreateRequest request) {

        QuotationResponseDTO quotation = quotationService.createQuotation(request);
        ApiResponse<QuotationResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Quotation created successfully",
                quotation
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing quotation
     */
    @PutMapping("/{quotationId}")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> updateQuotation(
            @PathVariable @Min(1) Long quotationId,
            @RequestParam @Min(1) Long companyId,
            @Valid @RequestBody QuotationUpdateRequest request) {

        QuotationResponseDTO quotation = quotationService.updateQuotation(quotationId, companyId, request);
        ApiResponse<QuotationResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotation updated successfully",
                quotation
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get quotation by ID
     */
    @GetMapping("/{quotationId}")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> getQuotation(
            @PathVariable @Min(1) Long quotationId,
            @RequestParam @Min(1) Long companyId) {

        QuotationResponseDTO quotation = quotationService.getQuotationById(quotationId, companyId);
        ApiResponse<QuotationResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotation retrieved successfully",
                quotation
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all quotations for a company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<QuotationResponseDTO>>> getAllQuotationsByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<QuotationResponseDTO> quotations = quotationService.getAllQuotationsByCompany(companyId);
        ApiResponse<List<QuotationResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotations retrieved successfully",
                quotations
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get quotations by customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<QuotationResponseDTO>>> getQuotationsByCustomer(
            @PathVariable @Min(1) Long customerId,
            @RequestParam @Min(1) Long companyId) {

        List<QuotationResponseDTO> quotations = quotationService.getQuotationsByCustomer(customerId, companyId);
        ApiResponse<List<QuotationResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Customer quotations retrieved successfully",
                quotations
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get quotations by status
     */
    @GetMapping("/company/{companyId}/status/{status}")
    public ResponseEntity<ApiResponse<List<QuotationResponseDTO>>> getQuotationsByStatus(
            @PathVariable @Min(1) Long companyId,
            @PathVariable String status) {

        List<QuotationResponseDTO> quotations = quotationService.getQuotationsByStatus(companyId, QuotationStatus.valueOf(status));
        ApiResponse<List<QuotationResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotations by status retrieved successfully",
                quotations
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete quotation (soft delete)
     */
   @DeleteMapping("/{quotationId}")
    public ResponseEntity<ApiResponse<String>> deleteQuotation(
            @PathVariable @Min(1) Long quotationId,
            @RequestParam @Min(1) Long companyId) {

        quotationService.deleteQuotation(quotationId, companyId);
        ApiResponse<String> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotation deleted successfully",
                "Quotation with ID " + quotationId + " has been deleted"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Update quotation status
     */
   /* @PatchMapping("/{quotationId}/status")
    public ResponseEntity<ApiResponse<QuotationResponseDTO>> updateQuotationStatus(
            @PathVariable @Min(1) Long quotationId,
            @RequestParam @Min(1) Long companyId,
            @RequestParam String status) {

        QuotationResponseDTO quotation = quotationService.updateQuotationStatus(quotationId, companyId, status);
        ApiResponse<QuotationResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotation status updated successfully",
                quotation
        );
        return ResponseEntity.ok(response);
    }*/


}