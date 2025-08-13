package com.xeine.services;


import com.xeine.dto.request.ProductCreateRequest;
import com.xeine.dto.request.ProductUpdateRequest;
import com.xeine.dto.response.ProductResponseDTO;
import com.xeine.enums.BusinessType;
import com.xeine.exception.*;
import com.xeine.models.Company;
import com.xeine.models.Product;
import com.xeine.repository.CompanyRepository;
import com.xeine.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateRequest request) {
        log.info("Creating product/service: {} for company: {}", request.getProductName(), request.getCompanyId());

        // Validate company exists and is active
        Company company = companyRepository.findActiveById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Active company not found with ID: " + request.getCompanyId()));



        // Check for duplicate product name
        if (productRepository.existsByNameAndCompany(request.getProductName(), request.getCompanyId())) {
            throw new DuplicateResourceException("Product name already exists: " + request.getProductName());
        }


        if (request.getBarcode() != null && !request.getBarcode().trim().isEmpty()) {
            if (productRepository.existsByBarcodeAndCompany(request.getBarcode().trim(), request.getCompanyId())) {
                throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
            }
        }


        Product product = new Product();
        product.setProductName(request.getProductName().trim());
        product.setDescription(request.getDescription());

        // Set new fields
        product.setHsnCode(request.getHsnCode());
        product.setBarcode(request.getBarcode() != null ? request.getBarcode().trim() : null);
        product.setUnitOfMeasure(request.getUnitOfMeasure());

        // Set categories with defaults
        product.setCategory(request.getCategory() != null && !request.getCategory().trim().isEmpty()
                ? request.getCategory().trim() : "General");
        product.setSubcategory(request.getSubcategory() != null && !request.getSubcategory().trim().isEmpty()
                ? request.getSubcategory().trim() : "Miscellaneous");

        product.setManufactureDate(request.getManufactureDate());
        product.setExpiryDate(request.getExpiryDate());

        // Existing fields
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setProductType(request.getProductType());
        product.setTaxRate(request.getTaxRate());
        product.setCompany(company);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());


        validateDates(request.getManufactureDate(), request.getExpiryDate());

        // Handle stock quantity based on product type
        handleStockQuantity(product, request.getStockQuantity());

        // Handle product image
        handleProductImage(product, request, request.getCompanyId());

        // Validate selling price > cost price
        validatePricing(product.getCostPrice(), product.getSellingPrice());

        Product savedProduct = productRepository.save(product);
        log.info("Successfully created product with ID: {}", savedProduct.getProductId());

        return convertToResponseDTO(savedProduct);
    }


    @Transactional
    public ProductResponseDTO updateProduct(Long productId, Long companyId, ProductUpdateRequest request) {
        log.info("Updating product ID: {} for company: {}", productId, companyId);

        Product existingProduct = productRepository.findActiveByIdAndCompany(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Validate unique product name if changed
        if (request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            if (!existingProduct.getProductName().equalsIgnoreCase(request.getProductName().trim()) &&
                    productRepository.existsByNameAndCompanyExcludingId(request.getProductName().trim(), companyId, productId)) {
                throw new DuplicateResourceException("Product name already exists: " + request.getProductName());
            }
        }

        // Validate unique barcode if changed
        if (request.getBarcode() != null && !request.getBarcode().trim().isEmpty()) {
            if (!request.getBarcode().trim().equals(existingProduct.getBarcode()) &&
                    productRepository.existsByBarcodeAndCompanyExcludingId(request.getBarcode().trim(), companyId, productId)) {
                throw new DuplicateResourceException("Barcode already exists: " + request.getBarcode());
            }
        }


        updateProductFields(existingProduct, request);


        handleProductImageUpdate(existingProduct, request, companyId);


        if (request.getCostPrice() != null || request.getSellingPrice() != null) {
            BigDecimal costPrice = request.getCostPrice() != null ? request.getCostPrice() : existingProduct.getCostPrice();
            BigDecimal sellingPrice = request.getSellingPrice() != null ? request.getSellingPrice() : existingProduct.getSellingPrice();
            validatePricing(costPrice, sellingPrice);
        }

        LocalDate manufactureDate = request.getManufactureDate() != null ? request.getManufactureDate() : existingProduct.getManufactureDate();
        LocalDate expiryDate = request.getExpiryDate() != null ? request.getExpiryDate() : existingProduct.getExpiryDate();
        validateDates(manufactureDate, expiryDate);

        existingProduct.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Successfully updated product with ID: {}", productId);
        return convertToResponseDTO(updatedProduct);
    }


    public ProductResponseDTO getProductById(Long productId, Long companyId) {
        Product product = productRepository.findActiveByIdAndCompany(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        return convertToResponseDTO(product);
    }

    public List<ProductResponseDTO> getAllProductsByCompany(Long companyId) {
        List<Product> products = productRepository.findByCompanyCompanyIdAndIsActiveTrue(companyId);
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    public List<ProductResponseDTO> getProductsByCompany(Long companyId) {
        List<Product> products = productRepository.findProductsByCompany(companyId);
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get services only (not products)
     */
    public List<ProductResponseDTO> getServicesByCompany(Long companyId) {
        List<Product> services = productRepository.findServicesByCompany(companyId);
        return services.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDTO updateStockQuantity(Long productId, Long companyId, Integer newQuantity) {
        Product product = productRepository.findActiveByIdAndCompany(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (product.getProductType() != BusinessType.PRODUCT) {
            throw new InvalidOperationException("Cannot update stock quantity for services");
        }

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newQuantity);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }


    @Transactional
    public void softDeleteProduct(Long productId, Long companyId) {
        Product product = productRepository.findActiveByIdAndCompany(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Successfully soft deleted product with ID: {}", productId);
    }


    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());

        dto.setHsnCode(product.getHsnCode());
        dto.setBarcode(product.getBarcode());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setCategory(product.getCategory());
        dto.setSubcategory(product.getSubcategory());
        dto.setManufactureDate(product.getManufactureDate());
        dto.setExpiryDate(product.getExpiryDate());


        dto.setCostPrice(product.getCostPrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setProductType(product.getProductType());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setTaxRate(product.getTaxRate());
        dto.setProductImage(product.getProductImage());
        dto.setCompanyId(product.getCompany().getCompanyId());
        dto.setCompanyName(product.getCompany().getCompanyName());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());


        dto.setProfitMargin(calculateProfitMargin(product));
        dto.setProfitAmount(product.getSellingPrice().subtract(product.getCostPrice()));




        return dto;
    }

    private void validatePricing(BigDecimal costPrice, BigDecimal sellingPrice) {
        if (sellingPrice.compareTo(costPrice) <= 0) {
            throw new BusinessValidationException("Selling price must be greater than cost price");
        }
    }

    @Transactional
    public ProductResponseDTO updateProductImage(Long productId, Long companyId, MultipartFile imageFile) {
        Product product = productRepository.findActiveByIdAndCompany(productId, companyId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        // Delete old image if exists
        if (product.getProductImage() != null) {
            imageUploadService.deleteImage(product.getProductImage());
        }

        // Upload new image
        String newImageUrl = imageUploadService.uploadImage(imageFile, "products");
        product.setProductImage(newImageUrl);
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return convertToResponseDTO(updatedProduct);
    }



    private void handleStockQuantity(Product product, Integer stockQuantity) {
        if (product.getProductType() == BusinessType.PRODUCT) {
            // For products, set stock quantity (default to 0 if not provided)
            product.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
        } else {
            // For services, stock quantity should be null
            product.setStockQuantity(null);
        }
    }




    private void handleProductImage(Product product, ProductCreateRequest request, Long companyId) {
        if (request.getProductImageFile() != null && !request.getProductImageFile().isEmpty()) {
            String imageUrl = imageUploadService.uploadImage(request.getProductImageFile(), "products");
            product.setProductImage(imageUrl);
        } else if (request.getProductImageUrl() != null && !request.getProductImageUrl().trim().isEmpty()) {
            product.setProductImage(request.getProductImageUrl().trim());
        }
    }

    private void handleProductImageUpdate(Product product, ProductUpdateRequest request, Long companyId) {
        if (request.getProductImageFile() != null && !request.getProductImageFile().isEmpty()) {
            // Delete old image if exists
            if (product.getProductImage() != null) {
                imageUploadService.deleteImage(product.getProductImage());
            }

            // Upload new image
            String imageUrl = imageUploadService.uploadImage(request.getProductImageFile(), "products");
            product.setProductImage(imageUrl);
        } else if (request.getProductImageUrl() != null && !request.getProductImageUrl().trim().isEmpty()) {
            product.setProductImage(request.getProductImageUrl().trim());
        }
    }

//    private void validatePricing(BigDecimal costPrice, BigDecimal sellingPrice) {
//        if (sellingPrice.compareTo(costPrice) <= 0) {
//            throw new BusinessValidationException("Selling price must be greater than cost price");
//        }
//    }

    private void updateProductFields(Product product, ProductUpdateRequest request) {
        if (request.getProductName() != null && !request.getProductName().trim().isEmpty()) {
            product.setProductName(request.getProductName().trim());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim().isEmpty() ? null : request.getDescription().trim());
        }

        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }

        if (request.getSellingPrice() != null) {
            product.setSellingPrice(request.getSellingPrice());
        }

        if (request.getTaxRate() != null) {
            product.setTaxRate(request.getTaxRate());
        }


        if (request.getStockQuantity() != null) {
            if (product.getProductType() == BusinessType.PRODUCT) {
                product.setStockQuantity(request.getStockQuantity());
            }

        }
    }

    private BigDecimal calculateProfitMargin(Product product) {
        if (product.getCostPrice().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profit = product.getSellingPrice().subtract(product.getCostPrice());
        return profit.divide(product.getCostPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDates(LocalDate manufactureDate, LocalDate expiryDate) {
        if (manufactureDate != null && expiryDate != null) {
            if (manufactureDate.isAfter(expiryDate)) {
                throw new BusinessValidationException("Manufacture date cannot be after expiry date");
            }
        }

        if (manufactureDate != null && manufactureDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Manufacture date cannot be in the future");
        }
    }


}