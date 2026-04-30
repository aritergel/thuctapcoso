package com.shoeshop.controller;

import com.shoeshop.dto.StatusUpdateRequest;
import com.shoeshop.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller quản trị — Tương đương routes/admin.js
 * Tất cả endpoint đều yêu cầu admin đăng nhập (kiểm tra session.adminId)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Helper kiểm tra quyền admin (tương đương middleware requireAdmin)
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("adminId") != null;
    }

    // ==================== SẢN PHẨM ====================

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        return ResponseEntity.ok(adminService.getProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(
            @RequestParam("ten_sp") String tenSp,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam("gia") String gia,
            @RequestParam(value = "gia_km", required = false) String giaKm,
            @RequestParam(value = "mo_ta_ngan", required = false) String moTaNgan,
            @RequestParam(value = "mo_ta_chi_tiet", required = false) String moTaChiTiet,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "so_luong", required = false) String soLuong,
            @RequestParam(value = "danh_muc_id", required = false) String danhMucId,
            @RequestParam(value = "hinh_anh", required = false) MultipartFile file,
            HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        try {
            adminService.createProduct(tenSp, slug, gia, giaKm, moTaNgan, moTaChiTiet, size, soLuong, danhMucId, file);
            return ResponseEntity.status(201).body(Map.of("message", "Thêm sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server", "error", e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @RequestParam("ten_sp") String tenSp,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam("gia") String gia,
            @RequestParam(value = "gia_km", required = false) String giaKm,
            @RequestParam(value = "mo_ta_ngan", required = false) String moTaNgan,
            @RequestParam(value = "mo_ta_chi_tiet", required = false) String moTaChiTiet,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "so_luong", required = false) String soLuong,
            @RequestParam(value = "danh_muc_id", required = false) String danhMucId,
            @RequestParam(value = "hinh_anh", required = false) MultipartFile file,
            HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        try {
            adminService.updateProduct(id, tenSp, slug, gia, giaKm, moTaNgan, moTaChiTiet, size, soLuong, danhMucId, file);
            return ResponseEntity.ok(Map.of("message", "Cập nhật sản phẩm thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        adminService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
    }

    // ==================== DANH MỤC ====================

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        return ResponseEntity.ok(adminService.getCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        adminService.createCategory(body.get("ten_danh_muc"), body.get("slug"), body.get("mo_ta"));
        return ResponseEntity.status(201).body(Map.of("message", "Thêm danh mục thành công"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Map<String, String> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        adminService.updateCategory(id, body.get("ten_danh_muc"), body.get("slug"), body.get("mo_ta"));
        return ResponseEntity.ok(Map.of("message", "Cập nhật danh mục thành công"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        adminService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Xóa danh mục thành công"));
    }

    // ==================== ĐƠN HÀNG ====================

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        return ResponseEntity.ok(adminService.getOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        Map<String, Object> result = adminService.getOrderDetail(id);
        if (result == null) return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer id,
                                                @RequestBody StatusUpdateRequest req, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        try {
            adminService.updateOrderStatus(id, req.getTrang_thai());
            return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("NOT_FOUND"))
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== KHÁCH HÀNG ====================

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        return ResponseEntity.ok(adminService.getCustomers());
    }

    // ==================== BÁO CÁO ====================

    @GetMapping("/report")
    public ResponseEntity<?> getReport(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(401).body(Map.of("message", "Không có quyền"));
        return ResponseEntity.ok(adminService.getReport());
    }
}
