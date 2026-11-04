
package com.example.session8.controller;

import com.example.session8.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public synchronized ResponseEntity<?> createProduct(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "price", required = false) String priceText,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        // 1. Kiem tra ten san pham
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tên sản phẩm không được để trống"));
        }

        // 2. Kiem tra gia san pham
        double price;

        try {
            price = Double.parseDouble(priceText);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Giá sản phẩm không hợp lệ"));
        }

        if (!Double.isFinite(price) || price < 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Giá sản phẩm phải lớn hơn hoặc bằng 0"));
        }

        // 3. Kiem tra anh bat buoc
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ảnh sản phẩm là bắt buộc"));
        }

        // 4. Kiem tra kich thuoc anh
        if (image.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ảnh không được vượt quá 5MB"));
        }

        // 5. Xac dinh dinh dang anh
        String extension;

        if ("image/jpeg".equals(image.getContentType())) {
            extension = ".jpg";
        } else if ("image/png".equals(image.getContentType())) {
            extension = ".png";
        } else if ("image/gif".equals(image.getContentType())) {
            extension = ".gif";
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Chỉ chấp nhận ảnh JPG, PNG hoặc GIF"));
        }

        // 6. Tao ten file ngau nhien bang UUID
        String fileName = "image_" + UUID.randomUUID() + extension;

        try {
            // 7. Tao thu muc uploads
            Files.createDirectories(UPLOAD_DIR);

            // 8. Luu file anh
            Path filePath = UPLOAD_DIR.resolve(fileName);

            Files.write(filePath, image.getBytes());

            // 9. Tao san pham va luu vao List
            Product product = new Product(
                    idGenerator.getAndIncrement(),
                    name.trim(),
                    price,
                    "uploads/" + fileName
            );

            products.add(product);

            // 10. Tra ve 201 Created
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(product);

        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể lưu ảnh sản phẩm"));
        }
    }

    @GetMapping
    public synchronized ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(new ArrayList<>(products));
    }
}
