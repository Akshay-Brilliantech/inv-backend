package com.xeine.controllers;

import com.xeine.dto.request.ProductCreateRequest;
import com.xeine.dto.request.ProductUpdateRequest;
import com.xeine.dto.response.ProductResponseDTO;
import com.xeine.services.ProductService;
import com.xeine.utils.responsehandler.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;


    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        ProductResponseDTO product = productService.createProduct(request);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Product created successfully",
                product
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping(value = "/with-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProductWithImage(
            @Valid @ModelAttribute ProductCreateRequest request) {

        ProductResponseDTO product = productService.createProduct(request);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.CREATED.value(),
                "Product with image created successfully",
                product
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllProductsByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<ProductResponseDTO> products = productService.getAllProductsByCompany(companyId);
        ApiResponse<List<ProductResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Products retrieved successfully",
                products
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/company/{companyId}/products")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getProductsByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<ProductResponseDTO> products = productService.getProductsByCompany(companyId);
        ApiResponse<List<ProductResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Products retrieved successfully",
                products
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get only services (not products) for a company
     */
    @GetMapping("/company/{companyId}/services")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getServicesByCompany(
            @PathVariable @Min(1) Long companyId) {

        List<ProductResponseDTO> services = productService.getServicesByCompany(companyId);
        ApiResponse<List<ProductResponseDTO>> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Services retrieved successfully",
                services
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProduct(
            @PathVariable @Min(1) Long productId,
            @RequestParam @Min(1) Long companyId) {

        ProductResponseDTO product = productService.getProductById(productId, companyId);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Product retrieved successfully",
                product
        );
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateStockQuantity(
            @PathVariable @Min(1) Long productId,
            @RequestParam @Min(1) Long companyId,
            @RequestParam @Min(0) Integer quantity) {

        ProductResponseDTO product = productService.updateStockQuantity(productId, companyId, quantity);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Stock quantity updated successfully",
                product
        );
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable @Min(1) Long productId,
            @RequestParam @Min(1) Long companyId,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponseDTO product = productService.updateProduct(productId, companyId, request);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Product updated successfully",
                product
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/with-image", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProductWithImage(
            @PathVariable @Min(1) Long productId,
            @RequestParam @Min(1) Long companyId,
            @Valid @ModelAttribute ProductUpdateRequest request) {

        ProductResponseDTO product = productService.updateProduct(productId, companyId, request);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Product with image updated successfully",
                product
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/image")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProductImage(
            @PathVariable @Min(1) Long productId,
            @RequestParam @Min(1) Long companyId,
            @RequestParam("image") MultipartFile imageFile) {

        ProductResponseDTO product = productService.updateProductImage(productId, companyId, imageFile);
        ApiResponse<ProductResponseDTO> response = new ApiResponse<>(
                true,
                HttpStatus.OK.value(),
                "Product image updated successfully",
                product
        );
        return ResponseEntity.ok(response);
    }





}
