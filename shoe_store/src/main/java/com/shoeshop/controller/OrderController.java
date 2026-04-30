package com.shoeshop.controller;

import com.shoeshop.dto.CheckoutRequest;
import com.shoeshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller đơn hàng — Tương đương routes/orders.js
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    @GetMapping
    public ResponseEntity<?> getOrders(HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            return ResponseEntity.ok(orderService.getOrders(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi server"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer id, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        Map<String, Object> result = orderService.getOrderDetail(id, userId);
        if (result == null) return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest req, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            Map<String, Object> result = orderService.checkout(userId, req.getDia_chi_giao_hang(), req.getGhi_chu());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id, HttpSession session) {
        Integer userId = getUserId(session);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        try {
            orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(Map.of("message", "Hủy đơn hàng thành công"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("NOT_FOUND"))
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
