package com.xeine.dto.response;

import com.xeine.enums.BusinessType;
import com.xeine.enums.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDTO {
    private Long companyId;
    private String mobile;
    private String email;
    private String companyName;
    private String role;
    private String gstNumber;
    private BusinessType businessType;
    private String address;
    private String country;
    private String state;
    private String city;
    private Long invoiceCount;
    private String companyImage;
    private Boolean isCompanyActive;
    private Boolean isOtpVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}

