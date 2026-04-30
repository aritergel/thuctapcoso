package com.shoeshop.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý đơn hàng
 * Tương đương logic trong routes/orders.js
 */
@Service
public class OrderService {

    private final JdbcTemplate db;

    public OrderService(JdbcTemplate db) {
        this.db = db;
    }

    public List<Map<String, Object>> getOrders(Integer userId) {
        List<Map<String, Object>> orders = db.queryForList(
                "SELECT * FROM DonHang WHERE khach_hang_id = ? ORDER BY ngay_dat DESC", userId);
        for (Map<String, Object> order : orders) {
            List<Map<String, Object>> items = db.queryForList(
                    "SELECT ctdh.*, sp.ten_sp, sp.hinh_anh, sp.slug FROM ChiTietDonHang ctdh " +
                    "JOIN SanPham sp ON ctdh.san_pham_id = sp.id WHERE ctdh.don_hang_id = ?",
                    order.get("id"));
            order.put("items", items);
        }
        return orders;
    }

    public Map<String, Object> getOrderDetail(Integer orderId, Integer userId) {
        List<Map<String, Object>> orders = db.queryForList(
                "SELECT * FROM DonHang WHERE id = ? AND khach_hang_id = ?", orderId, userId);
        if (orders.isEmpty()) return null;

        List<Map<String, Object>> items = db.queryForList(
                "SELECT ctdh.*, sp.ten_sp, sp.hinh_anh, sp.slug FROM ChiTietDonHang ctdh " +
                "JOIN SanPham sp ON ctdh.san_pham_id = sp.id WHERE ctdh.don_hang_id = ?", orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", orders.get(0));
        result.put("items", items);
        return result;
    }

    @Transactional
    public Map<String, Object> checkout(Integer userId, String diaChiGiao, String ghiChu) {
        // 1. Lấy giỏ hàng
        List<Map<String, Object>> cartItems = db.queryForList(
                "SELECT c.*, sp.gia, sp.gia_km, sp.ten_sp, sp.so_luong as ton_kho " +
                "FROM Cart c JOIN SanPham sp ON c.san_pham_id = sp.id WHERE c.khach_hang_id = ?", userId);

        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng trống");

        // 2. Kiểm tra tồn kho
        for (Map<String, Object> item : cartItems) {
            int soLuong = (Integer) item.get("so_luong");
            int tonKho = (Integer) item.get("ton_kho");
            if (soLuong > tonKho) {
                throw new RuntimeException("Sản phẩm \"" + item.get("ten_sp") + "\" không đủ số lượng trong kho");
            }
        }

        // 3. Tính tổng tiền
        BigDecimal tongTien = BigDecimal.ZERO;
        for (Map<String, Object> item : cartItems) {
            BigDecimal price = item.get("gia_km") != null ? (BigDecimal) item.get("gia_km") : (BigDecimal) item.get("gia");
            tongTien = tongTien.add(price.multiply(BigDecimal.valueOf((Integer) item.get("so_luong"))));
        }

        // 4-5. Tạo mã đơn + lưu đơn hàng
        String maDon = "DH" + System.currentTimeMillis();
        db.update("INSERT INTO DonHang (ma_don, khach_hang_id, tong_tien, trang_thai, dia_chi_giao, ghi_chu) VALUES (?, ?, ?, 0, ?, ?)",
                maDon, userId, tongTien, diaChiGiao, ghiChu);
        Integer donHangId = db.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        // 6. Lưu chi tiết + giảm tồn kho
        for (Map<String, Object> item : cartItems) {
            BigDecimal price = item.get("gia_km") != null ? (BigDecimal) item.get("gia_km") : (BigDecimal) item.get("gia");
            db.update("INSERT INTO ChiTietDonHang (don_hang_id, san_pham_id, so_luong, size, gia) VALUES (?, ?, ?, ?, ?)",
                    donHangId, item.get("san_pham_id"), item.get("so_luong"), item.get("size"), price);
            db.update("UPDATE SanPham SET so_luong = so_luong - ? WHERE id = ?",
                    item.get("so_luong"), item.get("san_pham_id"));
        }

        // 7. Xóa giỏ hàng
        db.update("DELETE FROM Cart WHERE khach_hang_id = ?", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Đặt hàng thành công");
        result.put("maDon", maDon);
        result.put("orderId", donHangId);
        return result;
    }

    @Transactional
    public void cancelOrder(Integer orderId, Integer userId) {
        List<Map<String, Object>> orders = db.queryForList(
                "SELECT * FROM DonHang WHERE id = ? AND khach_hang_id = ?", orderId, userId);
        if (orders.isEmpty()) throw new RuntimeException("NOT_FOUND");

        if ((Integer) orders.get(0).get("trang_thai") != 0)
            throw new RuntimeException("Không thể hủy đơn hàng này");

        // Hoàn lại tồn kho
        List<Map<String, Object>> items = db.queryForList(
                "SELECT san_pham_id, so_luong FROM ChiTietDonHang WHERE don_hang_id = ?", orderId);
        for (Map<String, Object> item : items) {
            db.update("UPDATE SanPham SET so_luong = so_luong + ? WHERE id = ?",
                    item.get("so_luong"), item.get("san_pham_id"));
        }

        db.update("UPDATE DonHang SET trang_thai = 3 WHERE id = ?", orderId);
    }
}
