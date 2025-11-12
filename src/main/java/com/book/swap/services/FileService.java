package com.book.swap.services;

import com.book.swap.models.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    public static final String UPLOAD_DIR =
            System.getProperty("user.dir") + File.separator + "Uploads";

    public FileService() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    // ✅ Upload File and return public URL
    public ApiResponse<String> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Generate unique filename
        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String uniqueName = UUID.randomUUID() + extension;
        String filePath = UPLOAD_DIR + File.separator + uniqueName;

        try (FileOutputStream fout = new FileOutputStream(filePath)) {
            fout.write(file.getBytes());

            // Build dynamic URL automatically (respects /api context-path)
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/uploads/")
                    .path(uniqueName)
                    .toUriString();

            return ApiResponse.<String>builder()
                    .success(true)
                    .message("File uploaded successfully")
                    .data(downloadUrl)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    // ✅ Get all files
    public List<String> getFiles() {
        File directory = new File(UPLOAD_DIR);
        String[] fileList = directory.list();

        if (fileList == null || fileList.length == 0) {
            return List.of();
        }

        // Build base URL dynamically (e.g., http://localhost:8080/api/files/uploads/download/)
        String baseDownloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/uploads/")
                .toUriString();

        // Map each filename to its full URL
        return Arrays.stream(fileList)
                .map(filename -> baseDownloadUrl + filename)
                .toList();
    }

    // ✅ Serve file (inline display)
    public ResponseEntity<InputStreamResource> serveFile(String filename) {
        File file = new File(UPLOAD_DIR + File.separator + filename);

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.status(404).body(null);
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Error serving file: " + filename, e);
        }
    }

    // ✅ Delete File
    public ApiResponse<String> deleteFile(String filename) {
        File file = new File(UPLOAD_DIR + File.separator + filename);

        if (!file.exists() || !file.isFile()) {
            return ApiResponse.<String>builder()
                    .success(false)
                    .message("File not found: " + filename)
                    .data(null)
                    .build();
        }

        boolean deleted = file.delete();
        if (deleted) {
            return ApiResponse.<String>builder()
                    .success(true)
                    .message("File deleted successfully")
                    .data(filename)
                    .build();
        } else {
            return ApiResponse.<String>builder()
                    .success(false)
                    .message("Failed to delete file: " + filename)
                    .data(null)
                    .build();
        }
    }
}
