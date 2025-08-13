package com.xeine.controllers;

import com.xeine.dto.response.CompanyReportDTO;
import com.xeine.dto.response.InvoiceResponseDTO;
import com.xeine.dto.response.ProductResponseDTO;
import com.xeine.dto.response.QuotationResponseDTO;
import com.xeine.dto.response.SettlementResponseDTO;
import com.xeine.services.ReportService;
import com.xeine.utils.responsehandler.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getInvoiceReport(
            @RequestParam Long companyId,
            @RequestParam(required = false) String period // week, month, quarter
    ) {
        if (period == null || period.isBlank()) {
            period = "week";
        }
        List<InvoiceResponseDTO> report = reportService.getInvoiceReport(companyId, period);
        ApiResponse<List<InvoiceResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Invoice report generated successfully",
                report
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company")
    public ResponseEntity<ApiResponse<CompanyReportDTO>> getCompanyReport(@RequestParam Long companyId) {
        CompanyReportDTO report = reportService.getCompanyReport(companyId);
        ApiResponse<CompanyReportDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Company report generated successfully",
                report
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getProductReport(
            @RequestParam Long companyId,
            @RequestParam(required = false) String period // week, month, quarter
    ) {
        if (period == null || period.isBlank()) {
            period = "week";
        }
        List<ProductResponseDTO> report = reportService.getProductReport(companyId, period);
        ApiResponse<List<ProductResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Product/Service report generated successfully",
                report
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getSettlementReport(
            @RequestParam Long companyId,
            @RequestParam(required = false) String period // week, month, quarter
    ) {
        if (period == null || period.isBlank()) {
            period = "week";
        }
        List<SettlementResponseDTO> report = reportService.getSettlementReport(companyId, period);
        ApiResponse<List<SettlementResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Settlement report generated successfully",
                report
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quotations")
    public ResponseEntity<ApiResponse<List<QuotationResponseDTO>>> getQuotationReport(
            @RequestParam Long companyId,
            @RequestParam(required = false) String period // week, month, quarter
    ) {
        if (period == null || period.isBlank()) {
            period = "week";
        }
        List<QuotationResponseDTO> report = reportService.getQuotationReport(companyId, period);
        ApiResponse<List<QuotationResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Quotation report generated successfully",
                report
        );
        return ResponseEntity.ok(response);
    }

    // Future: Add endpoints for other reports, etc.
}
