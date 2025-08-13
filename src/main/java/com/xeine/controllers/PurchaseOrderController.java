package com.xeine.controllers;

import com.xeine.dto.request.PurchaseOrderCreateRequest;
import com.xeine.dto.request.PurchaseOrderUpdateRequest;
import com.xeine.dto.response.PurchaseOrderResponseDTO;
import com.xeine.services.PurchaseOrderService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@Validated
@Slf4j
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * Create a new purchase order
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderCreateRequest request) {

        PurchaseOrderResponseDTO purchaseOrder = purchaseOrderService.createPurchaseOrder(request);
        ApiResponse<PurchaseOrderResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Purchase order created successfully",
                purchaseOrder
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update purchase order
     */
    @PutMapping("/{poId}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> updatePurchaseOrder(
            @PathVariable @Min(1) Long poId,
            @RequestParam @Min(1) Long companyId,
            @Valid @RequestBody PurchaseOrderUpdateRequest request) {

        PurchaseOrderResponseDTO purchaseOrder = purchaseOrderService.updatePurchaseOrder(poId, companyId, request);
        ApiResponse<PurchaseOrderResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase order updated successfully",
                purchaseOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get purchase order by ID
     */
    @GetMapping("/{poId}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> getPurchaseOrder(
            @PathVariable @Min(1) Long poId,
            @RequestParam @Min(1) Long companyId) {

        PurchaseOrderResponseDTO purchaseOrder = purchaseOrderService.getPurchaseOrderById(poId, companyId);
        ApiResponse<PurchaseOrderResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase order retrieved successfully",
                purchaseOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get purchase order by PO number
     */
    @GetMapping("/by-po-number/{poNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDTO>> getPurchaseOrderByPoNumber(
            @PathVariable String poNumber) {

        PurchaseOrderResponseDTO purchaseOrder = purchaseOrderService.getPurchaseOrderByPoNumber(poNumber);
        ApiResponse<PurchaseOrderResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase order retrieved successfully",
                purchaseOrder
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all purchase orders for a company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> getAllPurchaseOrdersByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<PurchaseOrderResponseDTO> purchaseOrders = purchaseOrderService.getAllPurchaseOrdersByCompany(companyId);
        ApiResponse<List<PurchaseOrderResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase orders retrieved successfully",
                purchaseOrders
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete purchase order (soft delete)
     */
    @DeleteMapping("/{poId}")
    public ResponseEntity<ApiResponse<String>> deletePurchaseOrder(
            @PathVariable @Min(1) Long poId,
            @RequestParam @Min(1) Long companyId) {

        purchaseOrderService.deletePurchaseOrder(poId, companyId);
        ApiResponse<String> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase order deleted successfully",
                "Purchase order with ID " + poId + " has been deleted"
        );
        return ResponseEntity.ok(response);
    }



    /**
     * Get purchase order statistics
     */
    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<ApiResponse<Object>> getPurchaseOrderStats(
            @PathVariable @Min(1) Long companyId) {

        List<PurchaseOrderResponseDTO> allPOs = purchaseOrderService.getAllPurchaseOrdersByCompany(companyId);

        // Create simple stats
        Object stats = new Object() {
            public final long totalPurchaseOrders = allPOs.size();
            public final double totalValue = allPOs.stream()
                    .mapToDouble(po -> po.getTotalAmount().doubleValue())
                    .sum();
        };

        ApiResponse<Object> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase order statistics retrieved successfully",
                stats
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Search purchase orders by vendor name
     */
    @GetMapping("/company/{companyId}/search")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> searchPurchaseOrdersByVendor(
            @PathVariable @Min(1) Long companyId,
            @RequestParam String vendorName) {

        List<PurchaseOrderResponseDTO> purchaseOrders = purchaseOrderService.getPurchaseOrdersByVendor(companyId, vendorName);

        ApiResponse<List<PurchaseOrderResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase orders search completed successfully",
                purchaseOrders
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get purchase orders by date range
     */
    @GetMapping("/company/{companyId}/date-range")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDTO>>> getPurchaseOrdersByDateRange(
            @PathVariable @Min(1) Long companyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // For now, returning all company POs
        // This would need proper date parsing implementation in service layer
        List<PurchaseOrderResponseDTO> purchaseOrders = purchaseOrderService.getAllPurchaseOrdersByCompany(companyId);

        ApiResponse<List<PurchaseOrderResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Purchase orders retrieved successfully",
                purchaseOrders
        );
        return ResponseEntity.ok(response);
    }
}