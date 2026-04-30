package com.shoeshop.service;

import com.shoeshop.config.WebConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý quản trị (Admin)
 * Tương đương logic trong routes/admin.js
 */
@Service
public class AdminService {

    private final JdbcTemplate db;

    public AdminService(JdbcTemplate db) {
        this.db = db;
    }

    // ==================== SẢN PHẨM ====================

    public List<Map<String, Object>> getProducts() {
        return db.queryForList(
                "SELECT sp.*, dm.ten_danh_muc FROM SanPham sp " +
                "LEFT JOIN DanhMuc dm ON sp.danh_muc_id = dm.id ORDER BY sp.ngay_them DESC");
    }

    public int createProduct(String tenSp, String slug, String gia, String giaKm,
                             String moTaNgan, String moTaChiTiet, String size,
                             String soLuong, String danhMucId, MultipartFile file) throws IOException {
        String hinhAnh = saveUploadFile(file);
        return db.update(
                "INSERT INTO SanPham (ten_sp, slug, gia, gia_km, mo_ta_ngan, mo_ta_chi_tiet, hinh_anh, size, so_luong, danh_muc_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                tenSp, slug,
                gia != null && !gia.isEmpty() ? Double.parseDouble(gia) : 0,
                giaKm != null && !giaKm.isEmpty() ? Double.parseDouble(giaKm) : null,
                moTaNgan, moTaChiTiet, hinhAnh, size,
                soLuong != null && !soLuong.isEmpty() ? Integer.parseInt(soLuong) : 0,
                danhMucId != null && !danhMucId.isEmpty() ? Integer.parseInt(danhMucId) : null);
    }

    public void updateProduct(Integer id, String tenSp, String slug, String gia, String giaKm,
                              String moTaNgan, String moTaChiTiet, String size,
                              String soLuong, String danhMucId, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String hinhAnh = saveUploadFile(file);
            db.update("UPDATE SanPham SET ten_sp=?, slug=?, gia=?, gia_km=?, mo_ta_ngan=?, mo_ta_chi_tiet=?, " +
                      "hinh_anh=?, size=?, so_luong=?, danh_muc_id=? WHERE id=?",
                    tenSp, slug, Double.parseDouble(gia),
                    giaKm != null && !giaKm.isEmpty() ? Double.parseDouble(giaKm) : null,
                    moTaNgan, moTaChiTiet, hinhAnh, size,
                    soLuong != null ? Integer.parseInt(soLuong) : 0,
                    danhMucId != null && !danhMucId.isEmpty() ? Integer.parseInt(danhMucId) : null, id);
        } else {
            db.update("UPDATE SanPham SET ten_sp=?, slug=?, gia=?, gia_km=?, mo_ta_ngan=?, mo_ta_chi_tiet=?, " +
                      "size=?, so_luong=?, danh_muc_id=? WHERE id=?",
                    tenSp, slug, Double.parseDouble(gia),
                    giaKm != null && !giaKm.isEmpty() ? Double.parseDouble(giaKm) : null,
                    moTaNgan, moTaChiTiet, size,
                    soLuong != null ? Integer.parseInt(soLuong) : 0,
                    danhMucId != null && !danhMucId.isEmpty() ? Integer.parseInt(danhMucId) : null, id);
        }
    }

    public void deleteProduct(Integer id) {
        db.update("DELETE FROM SanPham WHERE id = ?", id);
    }

    // ==================== DANH MỤC ====================

    public List<Map<String, Object>> getCategories() {
        return db.queryForList(
                "SELECT dm.*, COUNT(sp.id) as so_san_pham FROM DanhMuc dm " +
                "LEFT JOIN SanPham sp ON dm.id = sp.danh_muc_id GROUP BY dm.id ORDER BY dm.ten_danh_muc");
    }

    public void createCategory(String tenDanhMuc, String slug, String moTa) {
        db.update("INSERT INTO DanhMuc (ten_danh_muc, slug, mo_ta) VALUES (?, ?, ?)",
                tenDanhMuc, slug, moTa);
    }

    public void updateCategory(Integer id, String tenDanhMuc, String slug, String moTa) {
        db.update("UPDATE DanhMuc SET ten_danh_muc=?, slug=?, mo_ta=? WHERE id=?",
                tenDanhMuc, slug, moTa, id);
    }

    public void deleteCategory(Integer id) {
        db.update("DELETE FROM DanhMuc WHERE id = ?", id);
    }

    // ==================== ĐƠN HÀNG ====================

    public List<Map<String, Object>> getOrders() {
        return db.queryForList(
                "SELECT dh.*, kh.ho_ten, kh.email, kh.sdt FROM DonHang dh " +
                "JOIN KhachHang kh ON dh.khach_hang_id = kh.id ORDER BY dh.ngay_dat DESC");
    }

    public Map<String, Object> getOrderDetail(Integer orderId) {
        List<Map<String, Object>> orders = db.queryForList(
                "SELECT dh.*, kh.ho_ten, kh.email, kh.sdt, kh.dia_chi FROM DonHang dh " +
                "JOIN KhachHang kh ON dh.khach_hang_id = kh.id WHERE dh.id = ?", orderId);
        if (orders.isEmpty()) return null;

        List<Map<String, Object>> items = db.queryForList(
                "SELECT ctdh.*, sp.ten_sp, sp.hinh_anh FROM ChiTietDonHang ctdh " +
                "JOIN SanPham sp ON ctdh.san_pham_id = sp.id WHERE ctdh.don_hang_id = ?", orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", orders.get(0));
        result.put("items", items);
        return result;
    }

    public void updateOrderStatus(Integer orderId, Integer trangThai) {
        List<Map<String, Object>> orders = db.queryForList(
                "SELECT trang_thai FROM DonHang WHERE id = ?", orderId);
        if (orders.isEmpty()) throw new RuntimeException("NOT_FOUND");
        if ((Integer) orders.get(0).get("trang_thai") == 2)
            throw new RuntimeException("Đơn hàng đã hoàn thành, không thể thay đổi trạng thái!");
        db.update("UPDATE DonHang SET trang_thai = ? WHERE id = ?", trangThai, orderId);
    }

    // ==================== KHÁCH HÀNG ====================

    public List<Map<String, Object>> getCustomers() {
        return db.queryForList(
                "SELECT kh.id, kh.ho_ten, kh.email, kh.sdt, kh.dia_chi, kh.ngay_dang_ky, " +
                "COUNT(dh.id) as so_don_hang, " +
                "COALESCE(SUM(CASE WHEN dh.trang_thai = 2 THEN dh.tong_tien ELSE 0 END), 0) as tong_chi_tieu " +
                "FROM KhachHang kh LEFT JOIN DonHang dh ON kh.id = dh.khach_hang_id " +
                "GROUP BY kh.id ORDER BY kh.ngay_dang_ky DESC");
    }

    // ==================== BÁO CÁO ====================

    public Map<String, Object> getReport() {
        Map<String, Object> report = new HashMap<>();

        report.put("doanh_thu", db.queryForObject(
                "SELECT COALESCE(SUM(tong_tien), 0) FROM DonHang WHERE trang_thai = 2", Object.class));
        report.put("tong_don", db.queryForObject(
                "SELECT COUNT(*) FROM DonHang", Integer.class));
        report.put("don_cho_xu_ly", db.queryForObject(
                "SELECT COUNT(*) FROM DonHang WHERE trang_thai = 0", Integer.class));
        report.put("tong_khach", db.queryForObject(
                "SELECT COUNT(*) FROM KhachHang", Integer.class));

        report.put("topProducts", db.queryForList(
                "SELECT sp.id, sp.ten_sp, sp.hinh_anh, SUM(ctdh.so_luong) AS tong_ban " +
                "FROM ChiTietDonHang ctdh JOIN SanPham sp ON ctdh.san_pham_id = sp.id " +
                "JOIN DonHang dh ON ctdh.don_hang_id = dh.id WHERE dh.trang_thai = 2 " +
                "GROUP BY ctdh.san_pham_id ORDER BY tong_ban DESC LIMIT 10"));

        report.put("recentOrders", db.queryForList(
                "SELECT dh.*, kh.ho_ten FROM DonHang dh " +
                "JOIN KhachHang kh ON dh.khach_hang_id = kh.id ORDER BY dh.ngay_dat DESC LIMIT 5"));

        return report;
    }

    // ==================== HELPER ====================

    private String saveUploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String filename = System.currentTimeMillis() + getExtension(file.getOriginalFilename());
        File dest = new File(WebConfig.UPLOAD_DIR + filename);
        file.transferTo(dest);
        return "/uploads/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf(".");
        return idx >= 0 ? filename.substring(idx) : "";
    }
}
