package com.xeine.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_challan_item")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryChallanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_challan_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_challan_id", nullable = false)
    private DeliveryChallan deliveryChallan;

    // Reference to original invoice item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id", nullable = false)
    private InvoiceItem invoiceItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Product Details (copied for record keeping)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Quantity
    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    // Pricing Information (from invoice item)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    /**
     * Helper method to copy data from invoice item
     */
    public void copyFromInvoiceItem(InvoiceItem invoiceItem) {
        this.invoiceItem = invoiceItem;
        this.product = invoiceItem.getProduct();
        this.productName = invoiceItem.getProduct().getProductName();
        this.description = invoiceItem.getDescription();
        this.quantity = invoiceItem.getQuantity();
        this.unitPrice = invoiceItem.getUnitPrice();
        this.taxRate = invoiceItem.getTaxRate();
        this.taxAmount = invoiceItem.getTaxAmount();
        this.lineTotal = invoiceItem.getLineTotal();
    }
}