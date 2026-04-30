package com.shoeshop.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý giỏ hàng
 * Tương đương logic trong routes/cart.js
 */
@Service
public class CartService {

    private final JdbcTemplate db;

    public CartService(JdbcTemplate db) {
        this.db = db;
    }

    /**
     * Lấy giỏ hàng của khách hàng
     * Tương đương GET / trong cart.js
     */
    public Map<String, Object> getCart(Integer userId) {
        String sql = "SELECT c.id, c.so_luong, c.size, sp.id as san_pham_id, " +
                "sp.ten_sp, sp.gia, sp.gia_km, sp.hinh_anh, sp.slug " +
                "FROM Cart c JOIN SanPham sp ON c.san_pham_id = sp.id " +
                "WHERE c.khach_hang_id = ?";

        List<Map<String, Object>> items = db.queryForList(sql, userId);

        // Tính tổng tiền (tương đương reduce trong Node.js)
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            BigDecimal price = item.get("gia_km") != null
                    ? (BigDecimal) item.get("gia_km")
                    : (BigDecimal) item.get("gia");
            int qty = (Integer) item.get("so_luong");
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        return result;
    }

    /**
     * Thêm sản phẩm vào giỏ
     * Tương đương POST / trong cart.js
     */
    public void addToCart(Integer userId, Integer productId, Integer quantity, String size) {
        // Kiểm tra sản phẩm tồn tại
        List<Map<String, Object>> product = db.queryForList(
                "SELECT * FROM SanPham WHERE id = ?", productId);
        if (product.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        // Kiểm tra đã có trong giỏ chưa (cùng SP + cùng size)
        List<Map<String, Object>> existing = db.queryForList(
                "SELECT * FROM Cart WHERE khach_hang_id = ? AND san_pham_id = ? AND size = ?",
                userId, productId, size);

        if (!existing.isEmpty()) {
            // Cập nhật số lượng (tương đương Node.js)
            db.update("UPDATE Cart SET so_luong = so_luong + ? WHERE id = ?",
                    quantity, existing.get(0).get("id"));
        } else {
            // Thêm mới
            db.update("INSERT INTO Cart (khach_hang_id, san_pham_id, so_luong, size) VALUES (?, ?, ?, ?)",
                    userId, productId, quantity, size);
        }
    }

    /**
     * Cập nhật số lượng item trong giỏ
     * Tương đương PUT /:id trong cart.js
     */
    public void updateCartItem(Integer cartId, Integer userId, Integer quantity) {
        if (quantity < 1) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }
        db.update("UPDATE Cart SET so_luong = ? WHERE id = ? AND khach_hang_id = ?",
                quantity, cartId, userId);
    }

    /**
     * Xóa 1 item khỏi giỏ
     * Tương đương DELETE /:id trong cart.js
     */
    public void removeCartItem(Integer cartId, Integer userId) {
        db.update("DELETE FROM Cart WHERE id = ? AND khach_hang_id = ?", cartId, userId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     * Tương đương DELETE / trong cart.js
     */
    public void clearCart(Integer userId) {
        db.update("DELETE FROM Cart WHERE khach_hang_id = ?", userId);
    }
}
