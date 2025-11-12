package com.book.swap.controllers;

import com.book.swap.models.dto.ApiResponse;
import com.book.swap.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files/uploads")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Upload file
    @PostMapping
    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    // List files
    @GetMapping
    public ResponseEntity<List<String>> getFiles() {
        return ResponseEntity.ok(fileService.getFiles());
    }

    // View or download file
    @GetMapping("/{filename:.+}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String filename) {
        return fileService.serveFile(filename); // we’ll add this helper in service
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable String filename) {
        return ResponseEntity.ok(fileService.deleteFile(filename));
    }
}
