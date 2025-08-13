package com.xeine.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryChallanCreateRequest {


    @JsonProperty("paymentMode")
    private String paymentMode;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @JsonProperty("notes")
    private String notes;

    @JsonProperty("attachmentUrl")
    private String attachmentUrl;

    @JsonProperty("attachmentName")
    private String attachmentName;

    @JsonProperty("createdBy")
    private String createdBy;
}