package com.xeine.services;

import com.xeine.dto.response.DeliveryChallanResponseDTO;
import com.xeine.dto.response.DeliveryChallanItemResponseDTO;
import com.xeine.models.*;
import com.xeine.repository.DeliveryChallanRepository;
import com.xeine.repository.InvoiceRepository;
import com.xeine.exception.InvoiceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeliveryChallanService {

    @Autowired
    private DeliveryChallanRepository deliveryChallanRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    /**
     * Create simple delivery challan from invoice
     */
    @Transactional
    public DeliveryChallanResponseDTO createDeliveryChallanFromInvoice(Long invoiceId, DeliveryChallan deliveryChallanRequest) {
        log.info("Creating simple delivery challan for invoice {}", invoiceId);

        // Fetch the invoice with all its items
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Check if delivery challan already exists for this invoice
        Optional<DeliveryChallan> existingChallan = deliveryChallanRepository.findByInvoiceId(invoiceId);
        if (existingChallan.isPresent()) {
            throw new RuntimeException("Delivery Challan already exists for this invoice");
        }

        // Create new delivery challan
        DeliveryChallan deliveryChallan = new DeliveryChallan();

        // Generate challan number
        deliveryChallan.setChallanNumber(generateChallanNumber(invoice.getCompany().getCompanyId()));

        // Copy data from invoice
        deliveryChallan.copyFromInvoice(invoice);

        // Set delivery date (default to current date if not provided)
        if (deliveryChallanRequest.getDeliveryDate() != null) {
            deliveryChallan.setDeliveryDate(deliveryChallanRequest.getDeliveryDate());
        } else {
            deliveryChallan.setDeliveryDate(LocalDate.now());
        }

        // Set simple fields from request
        deliveryChallan.setPaymentMode(deliveryChallanRequest.getPaymentMode());
        deliveryChallan.setNotes(deliveryChallanRequest.getNotes());
        deliveryChallan.setAttachmentUrl(deliveryChallanRequest.getAttachmentUrl());
        deliveryChallan.setAttachmentName(deliveryChallanRequest.getAttachmentName());
        deliveryChallan.setCreatedBy(deliveryChallanRequest.getCreatedBy());

        // Save delivery challan first
        deliveryChallan = deliveryChallanRepository.save(deliveryChallan);

        // Create delivery challan items from invoice items
        for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
            DeliveryChallanItem challanItem = new DeliveryChallanItem();
            challanItem.setDeliveryChallan(deliveryChallan);
            challanItem.copyFromInvoiceItem(invoiceItem);
            deliveryChallan.getDeliveryChallanItems().add(challanItem);
        }

        DeliveryChallan savedChallan = deliveryChallanRepository.save(deliveryChallan);

        log.info("Successfully created delivery challan {} for invoice {}",
                savedChallan.getChallanNumber(), invoice.getInvoiceNumber());

        return convertToResponseDTO(savedChallan);
    }

    /**
     * Get delivery challan by ID
     */
    public DeliveryChallanResponseDTO getDeliveryChallanById(Long id) {
        log.info("Retrieving delivery challan with ID: {}", id);

        DeliveryChallan deliveryChallan = deliveryChallanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found with ID: " + id));

        log.info("Successfully retrieved delivery challan: {}", deliveryChallan.getChallanNumber());
        return convertToResponseDTO(deliveryChallan);
    }

    /**
     * Get delivery challan by invoice ID
     */
    public DeliveryChallanResponseDTO getDeliveryChallanByInvoiceId(Long invoiceId) {
        log.info("Retrieving delivery challan for invoice ID: {}", invoiceId);

        DeliveryChallan deliveryChallan = deliveryChallanRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found for invoice ID: " + invoiceId));

        log.info("Successfully retrieved delivery challan: {} for invoice: {}",
                deliveryChallan.getChallanNumber(), invoiceId);
        return convertToResponseDTO(deliveryChallan);
    }

    /**
     * Get all delivery challans for a company
     */
    public List<DeliveryChallanResponseDTO> getDeliveryChallansByCompany(Long companyId) {
        log.info("Retrieving delivery challans for company: {}", companyId);

        List<DeliveryChallan> deliveryChallans = deliveryChallanRepository.findByInvoice_Company_CompanyIdOrderByCreatedAtDesc(companyId);

        List<DeliveryChallanResponseDTO> responseDTOs = deliveryChallans.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("Retrieved {} delivery challans for company: {}", responseDTOs.size(), companyId);
        return responseDTOs;
    }

    /**
     * Update delivery challan details
     */
    @Transactional
    public DeliveryChallanResponseDTO updateDeliveryChallan(Long challanId, DeliveryChallan updateRequest) {
        log.info("Updating delivery challan with ID: {}", challanId);

        DeliveryChallan existingChallan = deliveryChallanRepository.findById(challanId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found with ID: " + challanId));

        // Update simple fields
        if (updateRequest.getDeliveryDate() != null) {
            existingChallan.setDeliveryDate(updateRequest.getDeliveryDate());
        }
        if (updateRequest.getPaymentMode() != null) {
            existingChallan.setPaymentMode(updateRequest.getPaymentMode());
        }
        if (updateRequest.getNotes() != null) {
            existingChallan.setNotes(updateRequest.getNotes());
        }
        if (updateRequest.getAttachmentUrl() != null) {
            existingChallan.setAttachmentUrl(updateRequest.getAttachmentUrl());
        }
        if (updateRequest.getAttachmentName() != null) {
            existingChallan.setAttachmentName(updateRequest.getAttachmentName());
        }

        DeliveryChallan updatedChallan = deliveryChallanRepository.save(existingChallan);

        log.info("Successfully updated delivery challan: {}", updatedChallan.getChallanNumber());
        return convertToResponseDTO(updatedChallan);
    }

    /**
     * Get today's deliveries
     */
    public List<DeliveryChallanResponseDTO> getTodayDeliveries() {
        log.info("Retrieving today's deliveries for date: {}", LocalDate.now());

        List<DeliveryChallan> todayDeliveries = deliveryChallanRepository.findByDeliveryDateOrderByCreatedAtDesc(LocalDate.now());

        List<DeliveryChallanResponseDTO> responseDTOs = todayDeliveries.stream()
                .map(this::convertToResponseDTO)
                .toList();

        log.info("Retrieved {} deliveries for today", responseDTOs.size());
        return responseDTOs;
    }

    /**
     * Delete delivery challan
     */
    @Transactional
    public void deleteDeliveryChallan(Long challanId) {
        log.info("Deleting delivery challan with ID: {}", challanId);

        DeliveryChallan challan = deliveryChallanRepository.findById(challanId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found with ID: " + challanId));

        deliveryChallanRepository.delete(challan);
        log.info("Successfully deleted delivery challan: {}", challan.getChallanNumber());
    }

    /**
     * Generate unique challan number
     */
    private String generateChallanNumber(Long companyId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = deliveryChallanRepository.count() + 1;
        return String.format("DC%d-%s-%04d", companyId, dateStr, count);
    }

    /**
     * Convert DeliveryChallan entity to ResponseDTO
     */
    private DeliveryChallanResponseDTO convertToResponseDTO(DeliveryChallan deliveryChallan) {
        DeliveryChallanResponseDTO dto = new DeliveryChallanResponseDTO();

        // Basic delivery challan info
        dto.setDeliveryChallanId(deliveryChallan.getDeliveryChallanId());
        dto.setChallanNumber(deliveryChallan.getChallanNumber());

        // Invoice info
        Invoice invoice = deliveryChallan.getInvoice();
        if (invoice != null) {
            dto.setInvoiceId(invoice.getId());
            dto.setInvoiceNumber(invoice.getInvoiceNumber());

            // Company info (minimal)
            Company company = invoice.getCompany();
            if (company != null) {
                dto.setCompanyId(company.getCompanyId());
                dto.setCompanyName(company.getCompanyName());
                dto.setCompanyAddress(buildCompanyAddress(company));
                dto.setCompanyGstNumber(company.getGstNumber());
            }

            // Customer info (minimal)
            Customer customer = invoice.getCustomer();
            if (customer != null) {
                dto.setCustomerId(customer.getCustomerId());
                dto.setCustomerName(customer.getCustomerName());
                dto.setCustomerAddress(customer.getAddress());
                dto.setCustomerGstNumber(customer.getGstNumber());
            }

            // Financial totals from invoice
            dto.setSubtotal(invoice.getSubtotal());
            dto.setTaxAmount(invoice.getTaxAmount());
            dto.setDiscountAmount(invoice.getDiscountAmount());
            dto.setTotalAmount(invoice.getTotalAmount());
        }

        // Delivery challan specific fields
        dto.setDeliveryDate(deliveryChallan.getDeliveryDate());
        dto.setPaymentMode(deliveryChallan.getPaymentMode());
        dto.setNotes(deliveryChallan.getNotes());
        dto.setAttachmentUrl(deliveryChallan.getAttachmentUrl());
        dto.setAttachmentName(deliveryChallan.getAttachmentName());
        dto.setCreatedBy(deliveryChallan.getCreatedBy());

        // Convert items
        List<DeliveryChallanItemResponseDTO> itemDTOs = deliveryChallan.getDeliveryChallanItems().stream()
                .map(this::convertItemToResponseDTO)
                .toList();
        dto.setItems(itemDTOs);
        dto.setTotalItems(itemDTOs.size());

        // Metadata
        dto.setCreatedAt(deliveryChallan.getCreatedAt());
        dto.setUpdatedAt(deliveryChallan.getUpdatedAt());

        return dto;
    }

    /**
     * Convert DeliveryChallanItem entity to ResponseDTO
     */
    private DeliveryChallanItemResponseDTO convertItemToResponseDTO(DeliveryChallanItem item) {
        DeliveryChallanItemResponseDTO dto = new DeliveryChallanItemResponseDTO();

        dto.setId(item.getId());

        // Product info
        Product product = item.getProduct();
        if (product != null) {
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setProductType(String.valueOf(product.getProductType()));
            dto.setProductImage(product.getProductImage());
        }

        // Quantity and pricing
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTaxRate(item.getTaxRate());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setLineTotal(item.getLineTotal());
        dto.setDescription(item.getDescription());

        return dto;
    }

    /**
     * Build formatted company address
     */
    private String buildCompanyAddress(Company company) {
        StringBuilder address = new StringBuilder();

        if (company.getAddress() != null && !company.getAddress().trim().isEmpty()) {
            address.append(company.getAddress().trim());
        }

        if (company.getCity() != null && !company.getCity().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(company.getCity().trim());
        }

        if (company.getState() != null && !company.getState().trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(company.getState().trim());
        }

        if (company.getCountry() != null && !company.getCountry().trim().isEmpty()) {
            if (!address.isEmpty()) address.append(", ");
            address.append(company.getCountry().trim());
        }

        return address.toString();
    }
}

/*
package com.xeine.services;

import com.xeine.models.*;
import com.xeine.repository.DeliveryChallanRepository;
import com.xeine.repository.InvoiceRepository;

import com.xeine.exception.InvoiceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeliveryChallanService {

    @Autowired
    private DeliveryChallanRepository deliveryChallanRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    */
/**
     * Create simple delivery challan from invoice
     *//*

    @Transactional
    public DeliveryChallan createDeliveryChallanFromInvoice(Long invoiceId, DeliveryChallan deliveryChallanRequest) {
        log.info("Creating simple delivery challan for invoice {}", invoiceId);

        // Fetch the invoice with all its items
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with ID: " + invoiceId));

        // Check if delivery challan already exists for this invoice
        Optional<DeliveryChallan> existingChallan = deliveryChallanRepository.findByInvoiceId(invoiceId);
        if (existingChallan.isPresent()) {
            throw new RuntimeException("Delivery Challan already exists for this invoice");
        }

        // Create new delivery challan
        DeliveryChallan deliveryChallan = new DeliveryChallan();

        // Generate challan number
        deliveryChallan.setChallanNumber(generateChallanNumber(invoice.getCompany().getCompanyId()));

        // Copy data from invoice
        deliveryChallan.copyFromInvoice(invoice);

        // Set delivery date (default to current date if not provided)
        if (deliveryChallanRequest.getDeliveryDate() != null) {
            deliveryChallan.setDeliveryDate(deliveryChallanRequest.getDeliveryDate());
        } else {
            deliveryChallan.setDeliveryDate(LocalDate.now());
        }

        // Set simple fields from request
        deliveryChallan.setPaymentMode(deliveryChallanRequest.getPaymentMode());
        deliveryChallan.setNotes(deliveryChallanRequest.getNotes());
        deliveryChallan.setAttachmentUrl(deliveryChallanRequest.getAttachmentUrl());
        deliveryChallan.setAttachmentName(deliveryChallanRequest.getAttachmentName());
        deliveryChallan.setCreatedBy(deliveryChallanRequest.getCreatedBy());

        // Save delivery challan first
        deliveryChallan = deliveryChallanRepository.save(deliveryChallan);

        // Create delivery challan items from invoice items
        for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
            DeliveryChallanItem challanItem = new DeliveryChallanItem();
            challanItem.setDeliveryChallan(deliveryChallan);
            challanItem.copyFromInvoiceItem(invoiceItem);
            deliveryChallan.getDeliveryChallanItems().add(challanItem);
        }

        DeliveryChallan savedChallan = deliveryChallanRepository.save(deliveryChallan);

        log.info("Successfully created delivery challan {} for invoice {}",
                savedChallan.getChallanNumber(), invoice.getInvoiceNumber());

        return savedChallan;
    }

    */
/**
     * Get delivery challan by ID
     *//*

    public Optional<DeliveryChallan> getDeliveryChallanById(Long id) {
        return deliveryChallanRepository.findById(id);
    }

    */
/**
     * Get delivery challan by invoice ID
     *//*

    public Optional<DeliveryChallan> getDeliveryChallanByInvoiceId(Long invoiceId) {
        return deliveryChallanRepository.findByInvoiceId(invoiceId);
    }

    */
/**
     * Get all delivery challans for a company
     *//*

    public List<DeliveryChallan> getDeliveryChallansByCompany(Long companyId) {
        return deliveryChallanRepository.findByInvoice_Company_CompanyIdOrderByCreatedAtDesc(companyId);
    }



    */
/**
     * Update delivery challan details
     *//*

    @Transactional
    public DeliveryChallan updateDeliveryChallan(Long challanId, DeliveryChallan updateRequest) {
        DeliveryChallan existingChallan = deliveryChallanRepository.findById(challanId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found"));

        // Update simple fields
        if (updateRequest.getDeliveryDate() != null) {
            existingChallan.setDeliveryDate(updateRequest.getDeliveryDate());
        }
        if (updateRequest.getPaymentMode() != null) {
            existingChallan.setPaymentMode(updateRequest.getPaymentMode());
        }
        if (updateRequest.getNotes() != null) {
            existingChallan.setNotes(updateRequest.getNotes());
        }
        if (updateRequest.getAttachmentUrl() != null) {
            existingChallan.setAttachmentUrl(updateRequest.getAttachmentUrl());
        }
        if (updateRequest.getAttachmentName() != null) {
            existingChallan.setAttachmentName(updateRequest.getAttachmentName());
        }

        return deliveryChallanRepository.save(existingChallan);
    }



    public List<DeliveryChallan> getTodayDeliveries() {
        return deliveryChallanRepository.findByDeliveryDateOrderByCreatedAtDesc(LocalDate.now());
    }

    */
/**
     * Delete delivery challan
     *//*

    @Transactional
    public void deleteDeliveryChallan(Long challanId) {
        DeliveryChallan challan = deliveryChallanRepository.findById(challanId)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found"));

        deliveryChallanRepository.delete(challan);
        log.info("Deleted delivery challan {}", challan.getChallanNumber());
    }

    */
/**
     * Generate unique challan number
     *//*

    private String generateChallanNumber(Long companyId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = deliveryChallanRepository.count() + 1;
        return String.format("DC%d-%s-%04d", companyId, dateStr, count);
    }
}*/
