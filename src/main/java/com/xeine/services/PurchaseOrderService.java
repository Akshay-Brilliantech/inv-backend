package com.xeine.services;

import com.xeine.dto.request.*;
import com.xeine.dto.response.PurchaseOrderResponseDTO;
import com.xeine.dto.response.PurchaseOrderItemResponseDTO;
import com.xeine.exception.*;
import com.xeine.models.*;
import com.xeine.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Create a new purchase order
     */
    @Transactional
    public PurchaseOrderResponseDTO createPurchaseOrder(PurchaseOrderCreateRequest request) {
        log.info("Creating purchase order with PO number: {} for company: {} and vendor: {}",
                request.getPoNumber(), request.getCompanyId(), request.getVendorName());

        // Validate company
        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Active company not found with ID: " + request.getCompanyId()));

        // Check if PO number already exists
        if (purchaseOrderRepository.existsByPoNumber(request.getPoNumber())) {
            throw new BusinessValidationException("Purchase Order number already exists: " + request.getPoNumber());
        }

        // Create purchase order
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPoNumber(request.getPoNumber().trim());
        purchaseOrder.setCompany(company);
        purchaseOrder.setVendorName(request.getVendorName());
        purchaseOrder.setPoDate(request.getPoDate());
        purchaseOrder.setNotes(request.getNotes());
        purchaseOrder.setCreatedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedAt(LocalDateTime.now());

        // Process purchase order items and update stock
        processPurchaseOrderItems(purchaseOrder, request.getItems());

        // Calculate total
        calculatePurchaseOrderTotal(purchaseOrder);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        log.info("Successfully created purchase order with ID: {} and number: {}",
                savedPurchaseOrder.getPurchaseOrderId(), savedPurchaseOrder.getPoNumber());

        return convertToResponseDTO(savedPurchaseOrder);
    }

    /**
     * Update purchase order
     */
    @Transactional
    public PurchaseOrderResponseDTO updatePurchaseOrder(Long poId, Long companyId, PurchaseOrderUpdateRequest request) {
        log.info("Updating purchase order ID: {} for company: {}", poId, companyId);

        PurchaseOrder existingPO = purchaseOrderRepository.findByIdAndCompany(poId, companyId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));

        // Update fields
        updatePurchaseOrderFields(existingPO, request, poId);

        // Update items if provided
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Reduce stock for existing items before updating
            reduceStockForExistingItems(existingPO);

            // Clear existing items
            existingPO.getPurchaseOrderItems().clear();

            // Add new items and update stock
            processPurchaseOrderItems(existingPO, request.getItems());
        }

        // Recalculate total
        calculatePurchaseOrderTotal(existingPO);

        existingPO.setUpdatedAt(LocalDateTime.now());
        PurchaseOrder updatedPO = purchaseOrderRepository.save(existingPO);

        log.info("Successfully updated purchase order with ID: {}", poId);
        return convertToResponseDTO(updatedPO);
    }

    /**
     * Get purchase order by ID
     */
    public PurchaseOrderResponseDTO getPurchaseOrderById(Long poId, Long companyId) {
        log.info("Retrieving purchase order ID: {} for company: {}", poId, companyId);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndCompany(poId, companyId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));

        log.info("Successfully retrieved purchase order with ID: {} and number: {}",
                purchaseOrder.getPurchaseOrderId(), purchaseOrder.getPoNumber());

        return convertToResponseDTO(purchaseOrder);
    }

    /**
     * Get purchase order by PO number
     */
    public PurchaseOrderResponseDTO getPurchaseOrderByPoNumber(String poNumber) {
        log.info("Retrieving purchase order with PO number: {}", poNumber);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with PO number: " + poNumber));

        log.info("Successfully retrieved purchase order with PO number: {}", poNumber);
        return convertToResponseDTO(purchaseOrder);
    }

    /**
     * Get all purchase orders for a company
     */
    public List<PurchaseOrderResponseDTO> getAllPurchaseOrdersByCompany(Long companyId) {
        log.info("Retrieving all purchase orders for company: {}", companyId);

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllByCompanyId(companyId);

        List<PurchaseOrderResponseDTO> responseDTOs = purchaseOrders.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("Retrieved {} purchase orders for company: {}", responseDTOs.size(), companyId);
        return responseDTOs;
    }

    /**
     * Get purchase orders by vendor name
     */
    public List<PurchaseOrderResponseDTO> getPurchaseOrdersByVendor(Long companyId, String vendorName) {
        log.info("Retrieving purchase orders for vendor: {} and company: {}", vendorName, companyId);

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByVendorNameContainingAndCompanyId(vendorName, companyId);

        List<PurchaseOrderResponseDTO> responseDTOs = purchaseOrders.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("Retrieved {} purchase orders for vendor: {}", responseDTOs.size(), vendorName);
        return responseDTOs;
    }

    /**
     * Delete purchase order (soft delete)
     */
    @Transactional
    public void deletePurchaseOrder(Long poId, Long companyId) {
        log.info("Soft deleting purchase order ID: {} for company: {}", poId, companyId);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndCompany(poId, companyId)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID: " + poId));

        // Reduce stock for all items before deletion
        reduceStockForExistingItems(purchaseOrder);

        int deletedCount = purchaseOrderRepository.softDeleteByIdAndCompany(poId, companyId);

        if (deletedCount == 0) {
            throw new BusinessValidationException("Failed to delete purchase order with ID: " + poId);
        }

        log.info("Successfully soft deleted purchase order with ID: {}", poId);
    }

    /**
     * Get recent purchase orders
     */
    public List<PurchaseOrderResponseDTO> getRecentPurchaseOrders(Long companyId, int limit) {
        log.info("Retrieving recent {} purchase orders for company: {}", limit, companyId);

        List<PurchaseOrder> recentPOs;
        if (limit <= 5) {
            recentPOs = purchaseOrderRepository.findTop5ByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        } else {
            recentPOs = purchaseOrderRepository.findTop10ByCompanyCompanyIdOrderByCreatedAtDesc(companyId);
        }

        List<PurchaseOrderResponseDTO> responseDTOs = recentPOs.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("Retrieved {} recent purchase orders for company: {}", responseDTOs.size(), companyId);
        return responseDTOs;
    }

    // Private helper methods

    private void processPurchaseOrderItems(PurchaseOrder purchaseOrder, List<PurchaseOrderItemRequest> itemRequests) {
        for (PurchaseOrderItemRequest itemRequest : itemRequests) {
            // Validate product exists and belongs to company
            Product product = productRepository.findActiveByIdAndCompany(
                            itemRequest.getProductId(), purchaseOrder.getCompany().getCompanyId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setProduct(product);
            item.setOrderedQuantity(itemRequest.getOrderedQuantity());
            // Use product's cost price
            item.setUnitCost(product.getCostPrice());
            item.setDescription(itemRequest.getDescription());

            // Calculate line total directly
            BigDecimal lineTotal = itemRequest.getOrderedQuantity().multiply(product.getCostPrice());
            item.setLineTotal(lineTotal);

            // Update product stock - increase when creating PO
            updateProductStock(product, itemRequest.getOrderedQuantity(), true);

            // Add item to purchase order list directly
            purchaseOrder.getPurchaseOrderItems().add(item);
        }
    }

    private void calculatePurchaseOrderTotal(PurchaseOrder purchaseOrder) {
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseOrderItem item : purchaseOrder.getPurchaseOrderItems()) {
            total = total.add(item.getLineTotal());
        }

        purchaseOrder.setTotalAmount(total);
    }

    private void updatePurchaseOrderFields(PurchaseOrder purchaseOrder, PurchaseOrderUpdateRequest request, Long poId) {
        // Update PO number if provided and check for uniqueness
        if (request.getPoNumber() != null && !request.getPoNumber().trim().isEmpty()) {
            String newPoNumber = request.getPoNumber().trim();
            if (!newPoNumber.equals(purchaseOrder.getPoNumber())) {
                // Check if new PO number already exists (excluding current PO)
                if (purchaseOrderRepository.existsByPoNumberAndIdNot(newPoNumber, poId)) {
                    throw new BusinessValidationException("Purchase Order number already exists: " + newPoNumber);
                }
                purchaseOrder.setPoNumber(newPoNumber);
            }
        }

        if (request.getVendorName() != null && !request.getVendorName().trim().isEmpty()) {
            purchaseOrder.setVendorName(request.getVendorName().trim());
        }
        if (request.getPoDate() != null) {
            purchaseOrder.setPoDate(request.getPoDate());
        }
        if (request.getNotes() != null) {
            purchaseOrder.setNotes(request.getNotes());
        }
    }

    private void updateProductStock(Product product, BigDecimal quantity, boolean increase) {
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }

        int quantityInt = quantity.intValue();
        if (increase) {
            product.setStockQuantity(product.getStockQuantity() + quantityInt);
        } else {
            product.setStockQuantity(Math.max(0, product.getStockQuantity() - quantityInt));
        }
        product.setUpdatedAt(LocalDateTime.now());
    }

    private void reduceStockForExistingItems(PurchaseOrder purchaseOrder) {
        for (PurchaseOrderItem item : purchaseOrder.getPurchaseOrderItems()) {
            updateProductStock(item.getProduct(), item.getOrderedQuantity(), false);
        }
    }

    private PurchaseOrderResponseDTO convertToResponseDTO(PurchaseOrder purchaseOrder) {
        PurchaseOrderResponseDTO dto = new PurchaseOrderResponseDTO();

        dto.setPurchaseOrderId(purchaseOrder.getPurchaseOrderId());
        dto.setPoNumber(purchaseOrder.getPoNumber());

        // Company info
        dto.setCompanyId(purchaseOrder.getCompany().getCompanyId());
        dto.setCompanyName(purchaseOrder.getCompany().getCompanyName());

        // Vendor info
        dto.setVendorName(purchaseOrder.getVendorName());

        // Date
        dto.setPoDate(purchaseOrder.getPoDate());

        // Financial total
        dto.setTotalAmount(purchaseOrder.getTotalAmount());

        // Metadata
        dto.setNotes(purchaseOrder.getNotes());
        dto.setCreatedAt(purchaseOrder.getCreatedAt());
        dto.setUpdatedAt(purchaseOrder.getUpdatedAt());

        // Convert items
        List<PurchaseOrderItemResponseDTO> itemDTOs = purchaseOrder.getPurchaseOrderItems().stream()
                .map(this::convertItemToResponseDTO)
                .toList();
        dto.setItems(itemDTOs);

        // Computed fields
        dto.setTotalItems(itemDTOs.size());

        return dto;
    }

    private PurchaseOrderItemResponseDTO convertItemToResponseDTO(PurchaseOrderItem item) {
        PurchaseOrderItemResponseDTO dto = new PurchaseOrderItemResponseDTO();

        dto.setPurchaseOrderItemId(item.getPurchaseOrderItemId());

        // Product info
        Product product = item.getProduct();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductType(product.getProductType().toString());
        dto.setHsnCode(product.getHsnCode());

        // Quantity and pricing
        dto.setOrderedQuantity(item.getOrderedQuantity());
        dto.setUnitCost(item.getUnitCost());
        dto.setLineTotal(item.getLineTotal());

        // Additional info
        dto.setDescription(item.getDescription());

        return dto;
    }
}