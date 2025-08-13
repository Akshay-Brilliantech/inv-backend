package com.xeine.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_challan")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryChallan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_challan_id")
    private Long deliveryChallanId;

    @Column(name = "challan_number", nullable = false, unique = true)
    private String challanNumber;

    // Link to original invoice
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Company Details (copied from invoice for record keeping)
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "company_mobile")
    private String companyMobile;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "company_gst_number")
    private String companyGstNumber;

    // Customer Details (copied from invoice for record keeping)
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "customer_mobile")
    private String customerMobile;

    @Column(name = "customer_email")
    private String customerEmail;

    // Invoice Reference Details
    @Column(name = "invoice_number", nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    // Delivery Information
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    // Financial Information (from invoice)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;


    @Column(name = "payment_mode")
    private String paymentMode;

    // Additional Information
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Attachment
    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "attachment_name")
    private String attachmentName;



    // Delivery Confirmation
    @Column(name = "delivered_by")
    private String deliveredBy;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "delivery_confirmation_date")
    private LocalDateTime deliveryConfirmationDate;

    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    // Delivery Challan Items (copied from invoice items)
    @OneToMany(mappedBy = "deliveryChallan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryChallanItem> deliveryChallanItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to copy data from invoice
     */
    public void copyFromInvoice(Invoice invoice) {
        this.invoice = invoice;
        this.invoiceNumber = invoice.getInvoiceNumber();
        this.invoiceDate = invoice.getInvoiceDate();
        this.totalAmount = invoice.getTotalAmount();
        this.taxAmount = invoice.getTaxAmount();
        this.subtotal = invoice.getSubtotal();

        // Copy company details
        Company company = invoice.getCompany();
        this.companyName = company.getCompanyName();
        this.companyAddress = company.getAddress();
        this.companyMobile = company.getMobile();
        this.companyEmail = company.getEmail();
        this.companyGstNumber = company.getGstNumber();

        // Copy customer details
        Customer customer = invoice.getCustomer();
        this.customerName = customer.getCustomerName();
        this.customerAddress = customer.getAddress();
        this.customerMobile = customer.getMobile();
        this.customerEmail = customer.getEmail();
    }
}