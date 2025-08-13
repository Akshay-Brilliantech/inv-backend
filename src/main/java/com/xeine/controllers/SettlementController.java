package com.xeine.controllers;

import com.xeine.dto.request.SettlementCreateRequest;
import com.xeine.dto.response.SettlementResponseDTO;

import com.xeine.enums.PaymentMethod;
import com.xeine.services.SettlementService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    /**
     * Create a settlement (payment) for an invoice
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SettlementResponseDTO>> createSettlement(
            @Valid @RequestBody SettlementCreateRequest request) {

        SettlementResponseDTO settlement = settlementService.createSettlement(request);
        ApiResponse<SettlementResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Settlement created successfully",
                settlement
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SettlementResponseDTO>> getSettlement(
            @PathVariable Long settlementId,
            @RequestParam Long companyId) {

        SettlementResponseDTO settlement = settlementService.getSettlementById(settlementId, companyId);
        ApiResponse<SettlementResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Settlement retrieved successfully",
                settlement
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all settlements for a company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getSettlementsByCompany(
            @PathVariable Long companyId) {

        List<SettlementResponseDTO> settlements = settlementService.getSettlementsByCompany(companyId);
        ApiResponse<List<SettlementResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Settlements retrieved successfully",
                settlements
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get settlements by date range
     */
    @GetMapping("/company/{companyId}/date-range")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getSettlementsByDateRange(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SettlementResponseDTO> settlements = settlementService.getSettlementsByDateRange(companyId, startDate, endDate);
        ApiResponse<List<SettlementResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Settlements retrieved successfully",
                settlements
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get settlements by payment method
     */
    @GetMapping("/company/{companyId}/payment-method/{paymentMethod}")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getSettlementsByPaymentMethod(
            @PathVariable Long companyId,
            @PathVariable PaymentMethod paymentMethod) {

        List<SettlementResponseDTO> settlements = settlementService.getSettlementsByPaymentMethod(companyId, paymentMethod);
        ApiResponse<List<SettlementResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Settlements retrieved successfully",
                settlements
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get total settlements amount for a company
     */
    @GetMapping("/company/{companyId}/total-amount")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getTotalSettlementsAmount(
            @PathVariable Long companyId) {

        BigDecimal totalAmount = settlementService.getTotalSettlementsAmount(companyId);
        Map<String, BigDecimal> result = Map.of("totalAmount", totalAmount);

        ApiResponse<Map<String, BigDecimal>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Total settlements amount retrieved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get total settlements amount by payment method
     */
    @GetMapping("/company/{companyId}/total-amount/payment-method/{paymentMethod}")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getTotalSettlementsAmountByPaymentMethod(
            @PathVariable Long companyId,
            @PathVariable PaymentMethod paymentMethod) {

        BigDecimal totalAmount = settlementService.getTotalSettlementsAmountByPaymentMethod(companyId, paymentMethod);
        Map<String, BigDecimal> result = Map.of("totalAmount", totalAmount);

        ApiResponse<Map<String, BigDecimal>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Total settlements amount by payment method retrieved successfully",
                result
        );
        return ResponseEntity.ok(response);
    }
}