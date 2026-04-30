package com.shoeshop.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service xử lý xác thực (đăng ký, đăng nhập, profile)
 * Tương đương logic trong routes/auth.js
 */
@Service
public class AuthService {

    private final JdbcTemplate db;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate db) {
        this.db = db;
    }

    // ==================== KHÁCH HÀNG ====================

    /**
     * Đăng ký tài khoản mới
     * Tương đương POST /register trong auth.js
     */
    public void register(String hoTen, String email, String matKhau) {
        // Kiểm tra email đã tồn tại
        List<Map<String, Object>> existing = db.queryForList(
                "SELECT id FROM KhachHang WHERE email = ?", email);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Hash mật khẩu bằng BCrypt (tương đương bcryptjs.hashSync)
        String hashedPassword = encoder.encode(matKhau);

        db.update("INSERT INTO KhachHang (ho_ten, email, mat_khau) VALUES (?, ?, ?)",
                hoTen, email, hashedPassword);
    }

    /**
     * Đăng nhập khách hàng
     * Tương đương POST /login trong auth.js
     * @return Map chứa thông tin user nếu thành công, null nếu thất bại
     */
    public Map<String, Object> login(String email, String matKhau) {
        List<Map<String, Object>> users = db.queryForList(
                "SELECT * FROM KhachHang WHERE email = ?", email);

        if (users.isEmpty()) {
            return null;
        }

        Map<String, Object> user = users.get(0);
        String storedPassword = (String) user.get("mat_khau");

        // So sánh password (tương đương bcryptjs.compareSync)
        if (!encoder.matches(matKhau, storedPassword)) {
            return null;
        }

        return user;
    }

    /**
     * Lấy thông tin khách hàng theo ID
     * Tương đương GET /me trong auth.js
     */
    public Map<String, Object> getUserById(Integer userId) {
        List<Map<String, Object>> users = db.queryForList(
                "SELECT id, ho_ten, email, sdt, dia_chi FROM KhachHang WHERE id = ?", userId);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Cập nhật hồ sơ khách hàng
     * Tương đương PUT /profile trong auth.js
     */
    public void updateProfile(Integer userId, String hoTen, String sdt, String diaChi) {
        db.update("UPDATE KhachHang SET ho_ten = ?, sdt = ?, dia_chi = ? WHERE id = ?",
                hoTen, sdt, diaChi, userId);
    }

    // ==================== ADMIN ====================

    /**
     * Đăng nhập admin
     * Tương đương POST /admin/login trong auth.js
     */
    public Map<String, Object> adminLogin(String username, String password) {
        List<Map<String, Object>> admins = db.queryForList(
                "SELECT * FROM Admin WHERE username = ?", username);

        if (admins.isEmpty()) {
            return null;
        }

        Map<String, Object> admin = admins.get(0);
        String storedPassword = (String) admin.get("password");

        if (!encoder.matches(password, storedPassword)) {
            return null;
        }

        return admin;
    }

    /**
     * Lấy thông tin admin theo ID
     * Tương đương GET /admin/me trong auth.js
     */
    public Map<String, Object> getAdminById(Integer adminId) {
        List<Map<String, Object>> admins = db.queryForList(
                "SELECT id, username, ho_ten FROM Admin WHERE id = ?", adminId);
        return admins.isEmpty() ? null : admins.get(0);
    }
}
