package com.shoeshop.controller;

import com.shoeshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller danh mục — Tương đương routes/categories.js
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getCategories() {
        try {
            return ResponseEntity.ok(productService.getCategories());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server"));
        }
    }

    @GetMapping("/{slug}/products")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit) {
        try {
            Map<String, Object> result = productService.getProductsByCategory(slug, page, limit);
            if (result == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy danh mục"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server"));
        }
    }
}
