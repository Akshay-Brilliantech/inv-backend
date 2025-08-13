package com.xeine.models;

import com.xeine.enums.BusinessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="company_id")
    private Long companyId;

    @Column(unique = true,name="mobile_no")
    private String mobile;

    @Column(unique = true,name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="company_name")
    private String companyName;

    @Column(name="role")
    private String role = "COMPANY";


    @Column(name = "gst_number")
    private String gstNumber; // Optional



    @Column(name="address")
    private String address;

    @Column(name="country")
    private String country;

    @Column(name="state")
    private String state;

    @Column(name="city")
    private String city;

    @Column(name = "invoice_count", nullable = false)
    private Long invoiceCount = 0L;

    @Column(name = "company_image")
    private String company_Image;


    private String otp;

    private LocalDateTime otpGeneratedAt;
    private Boolean isOtpVerified = false;

    @Column(name = "is_company_active", nullable = false)
    private Boolean isCompanyActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quotation> quotations = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();



}
