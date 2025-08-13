package com.xeine.controllers;

import com.xeine.dto.request.DeliveryChallanCreateRequest;
import com.xeine.dto.response.DeliveryChallanResponseDTO;
import com.xeine.models.DeliveryChallan;
import com.xeine.services.DeliveryChallanService;
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
@RequestMapping("/api/delivery-challan")
@Validated
@Slf4j
public class DeliveryChallanController {

    @Autowired
    private DeliveryChallanService deliveryChallanService;

    /**
     * Create delivery challan from invoice
     */
    @PostMapping("/create-from-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<DeliveryChallanResponseDTO>> createDeliveryChallanFromInvoice(
            @PathVariable @Min(1) Long invoiceId,
            @Valid @RequestBody DeliveryChallanCreateRequest request) {

        log.info("Creating delivery challan for invoice {}", invoiceId);

        // Convert DTO to entity
        DeliveryChallan deliveryChallanRequest = new DeliveryChallan();

        deliveryChallanRequest.setPaymentMode(request.getPaymentMode());
        deliveryChallanRequest.setNotes(request.getNotes());
        deliveryChallanRequest.setAttachmentUrl(request.getAttachmentUrl());
        deliveryChallanRequest.setAttachmentName(request.getAttachmentName());
        deliveryChallanRequest.setCreatedBy(request.getCreatedBy());

        DeliveryChallanResponseDTO createdChallan = deliveryChallanService.createDeliveryChallanFromInvoice(invoiceId, deliveryChallanRequest);

        ApiResponse<DeliveryChallanResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Delivery challan created successfully",
                createdChallan
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get delivery challan by ID
     */
    @GetMapping("/{challanId}")
    public ResponseEntity<ApiResponse<DeliveryChallanResponseDTO>> getDeliveryChallanById(
            @PathVariable @Min(1) Long challanId) {

        DeliveryChallanResponseDTO challan = deliveryChallanService.getDeliveryChallanById(challanId);

        ApiResponse<DeliveryChallanResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challan retrieved successfully",
                challan
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get delivery challan by invoice ID
     */
    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<DeliveryChallanResponseDTO>> getDeliveryChallanByInvoiceId(
            @PathVariable @Min(1) Long invoiceId) {

        DeliveryChallanResponseDTO challan = deliveryChallanService.getDeliveryChallanByInvoiceId(invoiceId);

        ApiResponse<DeliveryChallanResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challan retrieved successfully",
                challan
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get all delivery challans for a company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<DeliveryChallanResponseDTO>>> getDeliveryChallansByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<DeliveryChallanResponseDTO> challans = deliveryChallanService.getDeliveryChallansByCompany(companyId);

        ApiResponse<List<DeliveryChallanResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challans retrieved successfully",
                challans
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Update delivery challan
     */
    @PutMapping("/{challanId}")
    public ResponseEntity<ApiResponse<DeliveryChallanResponseDTO>> updateDeliveryChallan(
            @PathVariable @Min(1) Long challanId,
            @Valid @RequestBody DeliveryChallanCreateRequest updateRequest) {

        // Convert DTO to entity
        DeliveryChallan updateEntity = new DeliveryChallan();

        updateEntity.setPaymentMode(updateRequest.getPaymentMode());
        updateEntity.setNotes(updateRequest.getNotes());
        updateEntity.setAttachmentUrl(updateRequest.getAttachmentUrl());
        updateEntity.setAttachmentName(updateRequest.getAttachmentName());

        DeliveryChallanResponseDTO updatedChallan = deliveryChallanService.updateDeliveryChallan(challanId, updateEntity);

        ApiResponse<DeliveryChallanResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challan updated successfully",
                updatedChallan
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get today's deliveries
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<DeliveryChallanResponseDTO>>> getTodayDeliveries() {

        List<DeliveryChallanResponseDTO> todayDeliveries = deliveryChallanService.getTodayDeliveries();

        ApiResponse<List<DeliveryChallanResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Today's deliveries retrieved successfully",
                todayDeliveries
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Delete delivery challan
     */
    @DeleteMapping("/{challanId}")
    public ResponseEntity<ApiResponse<String>> deleteDeliveryChallan(
            @PathVariable @Min(1) Long challanId) {

        deliveryChallanService.deleteDeliveryChallan(challanId);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challan deleted successfully",
                "Delivery challan with ID " + challanId + " has been deleted"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get delivery challans by date range
     */
    @GetMapping("/company/{companyId}/date-range")
    public ResponseEntity<ApiResponse<List<DeliveryChallanResponseDTO>>> getDeliveryChallansByDateRange(
            @PathVariable @Min(1) Long companyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // This method would need to be implemented in the service
        // For now, returning company challans
        List<DeliveryChallanResponseDTO> challans = deliveryChallanService.getDeliveryChallansByCompany(companyId);

        ApiResponse<List<DeliveryChallanResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challans retrieved successfully",
                challans
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get delivery challan statistics for a company
     */
    @GetMapping("/company/{companyId}/stats")
    public ResponseEntity<ApiResponse<Object>> getDeliveryChallanStats(
            @PathVariable @Min(1) Long companyId) {

        List<DeliveryChallanResponseDTO> challans = deliveryChallanService.getDeliveryChallansByCompany(companyId);

        // Create simple stats
        Object stats = new Object() {
            public final long totalChallans = challans.size();
            public final long todayDeliveries = deliveryChallanService.getTodayDeliveries().size();
        };

        ApiResponse<Object> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Delivery challan statistics retrieved successfully",
                stats
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Check if delivery challan exists for invoice
     */
    @GetMapping("/exists/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<Boolean>> checkDeliveryChallanExists(
            @PathVariable @Min(1) Long invoiceId) {

        try {
            deliveryChallanService.getDeliveryChallanByInvoiceId(invoiceId);

            ApiResponse<Boolean> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Delivery challan exists for this invoice",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Boolean> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "No delivery challan found for this invoice",
                    false
            );
            return ResponseEntity.ok(response);
        }
    }
}

/*
package com.xeine.controllers;

import com.xeine.dto.request.DeliveryChallanCreateRequest;
import com.xeine.models.DeliveryChallan;
import com.xeine.services.DeliveryChallanService;
import com.xeine.utils.responsehandler.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/delivery-challan")
@Slf4j
public class DeliveryChallanController {

    @Autowired
    private DeliveryChallanService deliveryChallanService;

    */
/**
     * Create delivery challan from invoice
     *//*

    @PostMapping("/create-from-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<DeliveryChallan>> createDeliveryChallanFromInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody DeliveryChallanCreateRequest request) {
        try {
            log.info("Creating delivery challan for invoice {}", invoiceId);

            // Convert DTO to entity
            DeliveryChallan deliveryChallanRequest = new DeliveryChallan();

            deliveryChallanRequest.setPaymentMode(request.getPaymentMode());
            deliveryChallanRequest.setNotes(request.getNotes());
            deliveryChallanRequest.setAttachmentUrl(request.getAttachmentUrl());
            deliveryChallanRequest.setAttachmentName(request.getAttachmentName());
            deliveryChallanRequest.setCreatedBy(request.getCreatedBy());

            DeliveryChallan createdChallan = deliveryChallanService.createDeliveryChallanFromInvoice(invoiceId, deliveryChallanRequest);

            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    true,
                    HttpStatus.CREATED.value(),
                    "Delivery challan created successfully",
                    createdChallan
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating delivery challan for invoice {}: {}", invoiceId, e.getMessage());
            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to create delivery challan: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    */
/**
     * Get delivery challan by ID
     *//*

    @GetMapping("/{challanId}")
    public ResponseEntity<ApiResponse<DeliveryChallan>> getDeliveryChallanById(@PathVariable Long challanId) {
        try {
            Optional<DeliveryChallan> challan = deliveryChallanService.getDeliveryChallanById(challanId);

            if (challan.isPresent()) {
                ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                        true,
                        HttpStatus.OK.value(),
                        "Delivery challan retrieved successfully",
                        challan.get()
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                        false,
                        HttpStatus.NOT_FOUND.value(),
                        "Delivery challan not found",
                        null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error retrieving delivery challan {}: {}", challanId, e.getMessage());
            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve delivery challan: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    */
/**
     * Get delivery challan by invoice ID
     *//*

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<DeliveryChallan>> getDeliveryChallanByInvoiceId(@PathVariable Long invoiceId) {
        try {
            Optional<DeliveryChallan> challan = deliveryChallanService.getDeliveryChallanByInvoiceId(invoiceId);

            if (challan.isPresent()) {
                ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                        true,
                        HttpStatus.OK.value(),
                        "Delivery challan retrieved successfully",
                        challan.get()
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                        false,
                        HttpStatus.NOT_FOUND.value(),
                        "No delivery challan found for this invoice",
                        null
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error retrieving delivery challan for invoice {}: {}", invoiceId, e.getMessage());
            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve delivery challan: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    */
/**
     * Get all delivery challans for a company
     *//*

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<DeliveryChallan>>> getDeliveryChallansByCompany(@PathVariable Long companyId) {
        try {
            List<DeliveryChallan> challans = deliveryChallanService.getDeliveryChallansByCompany(companyId);

            ApiResponse<List<DeliveryChallan>> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Delivery challans retrieved successfully",
                    challans
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving delivery challans for company {}: {}", companyId, e.getMessage());
            ApiResponse<List<DeliveryChallan>> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve delivery challans: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    */
/**
     * Update delivery challan
     *//*

    @PutMapping("/{challanId}")
    public ResponseEntity<ApiResponse<DeliveryChallan>> updateDeliveryChallan(
            @PathVariable Long challanId,
            @Valid @RequestBody DeliveryChallanCreateRequest updateRequest) {
        try {
            // Convert DTO to entity
            DeliveryChallan updateEntity = new DeliveryChallan();

            updateEntity.setPaymentMode(updateRequest.getPaymentMode());
            updateEntity.setNotes(updateRequest.getNotes());
            updateEntity.setAttachmentUrl(updateRequest.getAttachmentUrl());
            updateEntity.setAttachmentName(updateRequest.getAttachmentName());

            DeliveryChallan updatedChallan = deliveryChallanService.updateDeliveryChallan(challanId, updateEntity);

            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Delivery challan updated successfully",
                    updatedChallan
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating delivery challan {}: {}", challanId, e.getMessage());
            ApiResponse<DeliveryChallan> response = new ApiResponse<>(
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to update delivery challan: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    */
/**
     * Get today's deliveries
     *//*

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<DeliveryChallan>>> getTodayDeliveries() {
        try {
            List<DeliveryChallan> todayDeliveries = deliveryChallanService.getTodayDeliveries();

            ApiResponse<List<DeliveryChallan>> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Today's deliveries retrieved successfully",
                    todayDeliveries
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving today's deliveries: {}", e.getMessage());
            ApiResponse<List<DeliveryChallan>> response = new ApiResponse<>(
                    false,
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to retrieve today's deliveries: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    */
/**
     * Delete delivery challan
     *//*

    @DeleteMapping("/{challanId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteDeliveryChallan(@PathVariable Long challanId) {
        try {
            deliveryChallanService.deleteDeliveryChallan(challanId);

            Map<String, String> result = new HashMap<>();
            result.put("message", "Delivery challan deleted successfully");
            result.put("challanId", challanId.toString());

            ApiResponse<Map<String, String>> response = new ApiResponse<>(
                    true,
                    HttpStatus.OK.value(),
                    "Delivery challan deleted successfully",
                    result
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting delivery challan {}: {}", challanId, e.getMessage());
            ApiResponse<Map<String, String>> response = new ApiResponse<>(
                    false,
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to delete delivery challan: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
*/
