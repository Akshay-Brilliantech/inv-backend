// Image Upload Service with AWS S3 and Local Fallback
package com.xeine.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    @Autowired(required = false)
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${app.upload.dir:uploads}")
    private String localUploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${aws.cloudfront.url:}")
    private String cloudFrontUrl;

    /**
     * Upload image to S3 or local storage as fallback
     */
    public String uploadImage(MultipartFile file, String category) {
        try {

            System.out.println(file);
            // Validate file
            validateImageFile(file);

            // Try S3 upload first if enabled and configured
            if (s3Enabled && isS3Available()) {
                return uploadToS3(file, category);
            } else {
                log.info("S3 not available or disabled, using local storage");
                return uploadToLocal(file, category);
            }

        } catch (Exception e) {
            log.error("Failed to upload image: {}", e.getMessage());

            // Fallback to local if S3 fails
            if (s3Enabled) {
                log.warn("S3 upload failed, falling back to local storage");
                try {
                    return uploadToLocal(file, category);
                } catch (Exception localException) {
                    log.error("Local upload also failed: {}", localException.getMessage());
                    throw new RuntimeException("Both S3 and local upload failed", localException);
                }
            }

            throw new RuntimeException("Failed to upload image", e);
        }
    }

    /**
     * Delete image from S3 or local storage
     */
    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return;
            }

            // Check if it's an S3 URL
            if (isS3Url(imageUrl)) {
                deleteFromS3(imageUrl);
            } else {
                deleteFromLocal(imageUrl);
            }
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
            // Don't throw exception for delete failures
        }
    }

    /**
     * Upload to AWS S3
     */
    private String uploadToS3(MultipartFile file, String category) throws IOException {
        String key = generateS3Key(file, category);

        try {
            // Create metadata
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            metadata.setHeader("Cache-Control", "public, max-age=31536000"); // 1 year cache

            // Upload to S3
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata);
            amazonS3.putObject(putRequest);

            // Generate URL
            String imageUrl = generateS3Url(key);
            log.info("Successfully uploaded image to S3: {}", imageUrl);
            return imageUrl;

        } catch (AmazonServiceException e) {
            log.error("AWS S3 service error: {}", e.getMessage());
            throw new RuntimeException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Upload to local storage
     */
    private String uploadToLocal(MultipartFile file, String category) throws IOException {
        // Create directory if not exists
        Path categoryPath = Paths.get(localUploadDir, category);
        Files.createDirectories(categoryPath);

        // Generate unique filename
        String filename = generateLocalFilename(file);

        // Save file
        Path filePath = categoryPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        String imageUrl = baseUrl + "/api/images/" + category + "/" + filename;
        log.info("Successfully uploaded image to local storage: {}", imageUrl);
        return imageUrl;
    }

    /**
     * Delete from AWS S3
     */
    private void deleteFromS3(String imageUrl) {
        try {
            String key = extractS3KeyFromUrl(imageUrl);
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
            log.info("Successfully deleted image from S3: {}", imageUrl);
        } catch (AmazonServiceException e) {
            log.error("Failed to delete from S3: {}", e.getMessage());
        }
    }

    /**
     * Delete from local storage
     */
    private void deleteFromLocal(String imageUrl) {
        try {
            // Extract filename from URL
            String[] urlParts = imageUrl.split("/");
            if (urlParts.length < 2) return;

            String filename = urlParts[urlParts.length - 1];
            String category = urlParts[urlParts.length - 2];

            Path filePath = Paths.get(localUploadDir, category, filename);
            Files.deleteIfExists(filePath);
            log.info("Successfully deleted image from local storage: {}", imageUrl);
        } catch (IOException e) {
            log.error("Failed to delete from local storage: {}", e.getMessage());
        }
    }

    /**
     * Check if S3 is available and configured
     */
    private boolean isS3Available() {
        return amazonS3 != null &&
                bucketName != null &&
                !bucketName.trim().isEmpty() &&
                s3Enabled;
    }

    /**
     * Check if URL is from S3
     */
    private boolean isS3Url(String imageUrl) {
        return imageUrl.contains("amazonaws.com") ||
                imageUrl.contains("s3.") ||
                (cloudFrontUrl != null && !cloudFrontUrl.isEmpty() && imageUrl.startsWith(cloudFrontUrl));
    }

    /**
     * Generate S3 key
     */
    private String generateS3Key(MultipartFile file, String category) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        return category + "/" + UUID.randomUUID().toString() + extension;
    }

    /**
     * Generate local filename
     */
    private String generateLocalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Generate S3 URL
     */
    private String generateS3Url(String key) {
        if (cloudFrontUrl != null && !cloudFrontUrl.trim().isEmpty()) {
            return cloudFrontUrl + "/" + key;
        }
        return amazonS3.getUrl(bucketName, key).toString();
    }

    /**
     * Extract S3 key from URL
     */
    private String extractS3KeyFromUrl(String imageUrl) {
        if (cloudFrontUrl != null && !cloudFrontUrl.isEmpty() && imageUrl.startsWith(cloudFrontUrl)) {
            return imageUrl.substring(cloudFrontUrl.length() + 1);
        }

        // For direct S3 URLs
        if (imageUrl.contains(bucketName)) {
            int keyStart = imageUrl.indexOf(bucketName) + bucketName.length() + 1;
            return imageUrl.substring(keyStart);
        }

        return imageUrl;
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("Invalid image type. Only JPEG, PNG, GIF, WebP allowed");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    /**
     * Check if content type is valid image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );
    }

    /**
     * Get storage type being used
     */
    public String getStorageType() {
        return isS3Available() ? "AWS S3" : "Local Storage";
    }

    /**
     * Check storage health
     */
    public boolean isStorageHealthy() {
        if (isS3Available()) {
            try {
                // Test S3 connection
                amazonS3.doesBucketExistV2(bucketName);
                return true;
            } catch (Exception e) {
                log.warn("S3 health check failed: {}", e.getMessage());
                return false;
            }
        } else {
            // Test local storage
            try {
                Path uploadPath = Paths.get(localUploadDir);
                Files.createDirectories(uploadPath);
                return Files.isWritable(uploadPath);
            } catch (Exception e) {
                log.warn("Local storage health check failed: {}", e.getMessage());
                return false;
            }
        }
    }
}