package com.xeine.dto.response;

import com.xeine.enums.CustomerType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomerResponseDTO {

    private Long customerId;
    private String customerName;
    private String email;
    private String mobile;
    private String address;
    private String gstNumber;
    private String customerImage;
    private CustomerType customerType;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


