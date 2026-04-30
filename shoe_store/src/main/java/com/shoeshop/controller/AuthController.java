package com.shoeshop.controller;

import com.shoeshop.dto.LoginRequest;
import com.shoeshop.dto.RegisterRequest;
import com.shoeshop.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xác thực — Tương đương routes/auth.js
 * Xử lý đăng ký, đăng nhập, profile cho cả Khách hàng và Admin
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== KHÁCH HÀNG ====================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            authService.register(req.getHo_ten(), req.getEmail(), req.getMat_khau());
            return ResponseEntity.status(201).body(Map.of("message", "Đăng ký thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        Map<String, Object> user = authService.login(req.getEmail(), req.getMat_khau());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Email hoặc mật khẩu không đúng"));
        }
        // Lưu session (tương đương req.session.userId = user.id trong Node.js)
        session.setAttribute("userId", user.get("id"));
        session.setAttribute("userName", user.get("ho_ten"));
        session.setAttribute("userEmail", user.get("email"));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng nhập thành công");
        response.put("user", Map.of("id", user.get("id"), "ho_ten", user.get("ho_ten"), "email", user.get("email")));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(Map.of("user", (Object) null));
        }
        Map<String, Object> user = authService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }
        authService.updateProfile(userId, body.get("ho_ten"), body.get("so_dien_thoai"), body.get("dia_chi"));
        return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
    }

    // ==================== ADMIN ====================

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> admin = authService.adminLogin(body.get("username"), body.get("password"));
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu"));
        }
        session.setAttribute("adminId", admin.get("id"));
        session.setAttribute("adminUsername", admin.get("username"));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng nhập thành công");
        response.put("admin", Map.of("id", admin.get("id"), "username", admin.get("username"), "ho_ten", admin.get("ho_ten")));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<?> adminLogout(HttpSession session) {
        session.removeAttribute("adminId");
        session.removeAttribute("adminUsername");
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @GetMapping("/admin/me")
    public ResponseEntity<?> getAdminMe(HttpSession session) {
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập admin"));
        }
        Map<String, Object> admin = authService.getAdminById(adminId);
        return ResponseEntity.ok(Map.of("admin", admin));
    }
}
