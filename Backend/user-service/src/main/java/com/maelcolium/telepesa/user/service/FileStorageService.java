package com.maelcolium.telepesa.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for handling file storage operations
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory to store uploaded files.", ex);
        }
    }

    /**
     * Store an avatar file and return the relative path
     */
    public String storeAvatar(MultipartFile file, Long userId) {
        // Validate file
        validateImageFile(file);

        // Generate unique filename
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getFileExtension(originalFileName);
        String fileName = "avatar_" + userId + "_" + UUID.randomUUID() + "." + fileExtension;

        try {
            // Create user-specific directory
            Path userDirectory = this.fileStorageLocation.resolve("avatars");
            Files.createDirectories(userDirectory);

            // Store file
            Path targetLocation = userDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Avatar uploaded successfully for user {} at: {}", userId, targetLocation);
            
            // Return relative URL path
            return "/uploads/avatars/" + fileName;

        } catch (IOException ex) {
            log.error("Failed to store avatar for user {}: {}", userId, ex.getMessage());
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Delete an avatar file
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith("/uploads/avatars/")) {
            return;
        }

        try {
            String fileName = avatarUrl.substring("/uploads/avatars/".length());
            Path filePath = this.fileStorageLocation.resolve("avatars").resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("Avatar deleted: {}", filePath);
        } catch (IOException ex) {
            log.error("Failed to delete avatar {}: {}", avatarUrl, ex.getMessage());
        }
    }

    /**
     * Validate that the uploaded file is a valid image
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        // Check file size (5MB max)
        long maxFileSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size (5MB)");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Check allowed extensions
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(fileName).toLowerCase();
        if (!isAllowedImageExtension(extension)) {
            throw new IllegalArgumentException("File type not allowed. Supported formats: jpg, jpeg, png, gif");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Check if image extension is allowed
     */
    private boolean isAllowedImageExtension(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) || 
               "png".equals(extension) || "gif".equals(extension);
    }
}
