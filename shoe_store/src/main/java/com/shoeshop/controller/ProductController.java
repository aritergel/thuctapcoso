package com.shoeshop.controller;

import com.shoeshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller sản phẩm — Tương đương routes/products.js
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lấy danh sách sản phẩm (phân trang, lọc, tìm kiếm, sắp xếp)
     * Tương đương GET / trong products.js
     */
    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String danh_muc,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String sort) {
        try {
            Map<String, Object> result = productService.getProducts(danh_muc, search, page, limit, sort);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server", "error", e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết sản phẩm theo slug hoặc ID
     * Tương đương GET /:slug trong products.js
     */
    @GetMapping("/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        try {
            Map<String, Object> result = productService.getProductBySlug(slug);
            if (result == null) {
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server", "error", e.getMessage()));
        }
    }
}
