package com.shoeshop.controller;

import com.shoeshop.dto.CartRequest;
import com.shoeshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller giỏ hàng — Tương đương routes/cart.js
 * Tất cả endpoint đều yêu cầu đăng nhập (kiểm tra session.userId)
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Helper kiểm tra đăng nhập (tương đương middleware requireLogin)
    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            return ResponseEntity.ok(cartService.getCart(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server"));
        }
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartRequest req, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            cartService.addToCart(userId, req.getSan_pham_id(), req.getSo_luong(), req.getSize());
            return ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ hàng"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable Integer id,
                                            @RequestBody CartRequest req, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            cartService.updateCartItem(id, userId, req.getSo_luong());
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Endpoint bổ sung cho cart.html (updateCartItemQty dùng POST /api/cart/update)
    @PostMapping("/update")
    public ResponseEntity<?> updateCartByDelta(@RequestBody CartRequest req, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            // Tìm cart item theo product_id
            Map<String, Object> cart = cartService.getCart(userId);
            var items = (java.util.List<Map<String, Object>>) cart.get("items");
            for (Map<String, Object> item : items) {
                if (item.get("san_pham_id").equals(req.getProduct_id())) {
                    int newQty = (Integer) item.get("so_luong") + (req.getDelta() != null ? req.getDelta() : 0);
                    if (newQty < 1) newQty = 1;
                    cartService.updateCartItem((Integer) item.get("id"), userId, newQty);
                    return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
                }
            }
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm trong giỏ"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeCartItem(@PathVariable Integer id, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        cartService.removeCartItem(id, userId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa khỏi giỏ hàng"));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ giỏ hàng"));
    }
}
