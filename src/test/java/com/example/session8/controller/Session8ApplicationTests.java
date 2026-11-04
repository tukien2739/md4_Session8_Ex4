package com.example.session8.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
public class Session8ApplicationTests {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    @PostMapping("/api/files/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam(value = "image", required = false)
            MultipartFile file) {

        // 1. Kiem tra nguoi dung co gui anh hay khong
        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Ảnh không được để trống"
                    ));
        }

        // 2. Lay ten goc cua file
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Tên file không hợp lệ"
                    ));
        }

        // Chi lay ten file, khong nhan duong dan
        String safeName = originalName.replace("\\", "/");
        safeName = safeName.substring(safeName.lastIndexOf('/') + 1);

        if (safeName.isBlank()
                || safeName.equals(".")
                || safeName.equals("..")
                || safeName.contains("..")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Tên file không hợp lệ"
                    ));
        }

        // 3. Luu file vao thu muc uploads
        try {
            Files.createDirectories(UPLOAD_DIR);

            Path target = UPLOAD_DIR.resolve(safeName);

            Files.copy(
                    file.getInputStream(),
                    target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // 4. Tra ve ket qua thanh cong
            return ResponseEntity.ok(Map.of(
                    "message", "Upload ảnh thành công",
                    "fileName", safeName,
                    "path", "uploads/" + safeName
            ));

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Không thể lưu file ảnh"
                    ));
        }
    }
}
