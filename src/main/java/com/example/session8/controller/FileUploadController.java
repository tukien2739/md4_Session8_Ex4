
package com.example.session8.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
public class FileUploadController {

    private static final Path UPLOAD_DIR =
            Paths.get("uploads");

    @PostMapping("/api/files/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam(value = "image", required = false)
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Ảnh không được để trống"
                    ));
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Tên file không hợp lệ"
                    ));
        }

        fileName = fileName.replace("\\", "/");
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        if (fileName.isBlank()
                || fileName.equals(".")
                || fileName.equals("..")
                || fileName.contains("..")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Tên file không hợp lệ"
                    ));
        }

        try {
            Files.createDirectories(UPLOAD_DIR);

            Path filePath = UPLOAD_DIR.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Upload thành công",
                    "fileName", fileName,
                    "path", "uploads/" + fileName
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Không thể lưu file"
                    ));
        }
    }
}
