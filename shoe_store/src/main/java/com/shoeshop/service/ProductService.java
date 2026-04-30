package com.shoeshop.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý sản phẩm và danh mục
 * Tương đương logic trong routes/products.js và routes/categories.js
 */
@Service
public class ProductService {

    private final JdbcTemplate db;

    public ProductService(JdbcTemplate db) {
        this.db = db;
    }

    // ==================== SẢN PHẨM ====================

    /**
     * Lấy danh sách sản phẩm có phân trang, lọc, tìm kiếm, sắp xếp
     * Tương đương GET / trong products.js
     */
    public Map<String, Object> getProducts(String danhMuc, String search,
                                            int page, int limit, String sort) {
        int offset = (page - 1) * limit;

        // Xây dựng câu query động (tương tự Node.js)
        StringBuilder sql = new StringBuilder(
                "SELECT sp.*, dm.ten_danh_muc FROM SanPham sp " +
                "LEFT JOIN DanhMuc dm ON sp.danh_muc_id = dm.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (danhMuc != null && !danhMuc.isEmpty()) {
            sql.append(" AND sp.danh_muc_id = ?");
            params.add(Integer.parseInt(danhMuc));
        }
        if (search != null && !search.isEmpty()) {
            sql.append(" AND sp.ten_sp LIKE ?");
            params.add("%" + search + "%");
        }

        // Sắp xếp (tương đương switch trong products.js)
        switch (sort != null ? sort : "newest") {
            case "price_asc" -> sql.append(" ORDER BY COALESCE(sp.gia_km, sp.gia) ASC");
            case "price_desc" -> sql.append(" ORDER BY COALESCE(sp.gia_km, sp.gia) DESC");
            case "name" -> sql.append(" ORDER BY sp.ten_sp ASC");
            case "popular" -> sql.append(" ORDER BY sp.luot_xem DESC");
            default -> sql.append(" ORDER BY sp.ngay_them DESC");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Map<String, Object>> products = db.queryForList(sql.toString(), params.toArray());

        // Đếm tổng (tương tự Node.js countSql)
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM SanPham sp WHERE 1=1");
        List<Object> countParams = new ArrayList<>();
        if (danhMuc != null && !danhMuc.isEmpty()) {
            countSql.append(" AND sp.danh_muc_id = ?");
            countParams.add(Integer.parseInt(danhMuc));
        }
        if (search != null && !search.isEmpty()) {
            countSql.append(" AND sp.ten_sp LIKE ?");
            countParams.add("%" + search + "%");
        }

        Integer total = db.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

        // Trả về format giống Node.js: { products, pagination }
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / limit));

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("pagination", pagination);
        return result;
    }

    /**
     * Lấy chi tiết sản phẩm theo slug hoặc id
     * Tương đương GET /:slug trong products.js
     */
    public Map<String, Object> getProductBySlug(String slug) {
        // Kiểm tra slug là số (id) hay text (slug) — giống Node.js
        boolean isId;
        try {
            Integer.parseInt(slug);
            isId = true;
        } catch (NumberFormatException e) {
            isId = false;
        }

        String sql = "SELECT sp.*, dm.ten_danh_muc FROM SanPham sp " +
                "LEFT JOIN DanhMuc dm ON sp.danh_muc_id = dm.id " +
                "WHERE " + (isId ? "sp.id = ?" : "sp.slug = ?");

        List<Map<String, Object>> rows = db.queryForList(sql, slug);
        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> product = rows.get(0);

        // Tăng lượt xem (tương đương Node.js)
        db.update("UPDATE SanPham SET luot_xem = luot_xem + 1 WHERE " +
                (isId ? "id = ?" : "slug = ?"), slug);

        // Lấy sản phẩm liên quan (cùng danh mục, tối đa 4)
        List<Map<String, Object>> related = db.queryForList(
                "SELECT * FROM SanPham WHERE danh_muc_id = ? AND id != ? LIMIT 4",
                product.get("danh_muc_id"), product.get("id"));

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("related", related);
        return result;
    }

    // ==================== DANH MỤC ====================

    /**
     * Lấy tất cả danh mục + đếm số SP
     * Tương đương GET / trong categories.js
     */
    public List<Map<String, Object>> getCategories() {
        return db.queryForList(
                "SELECT dm.*, COUNT(sp.id) as so_san_pham " +
                "FROM DanhMuc dm LEFT JOIN SanPham sp ON dm.id = sp.danh_muc_id " +
                "GROUP BY dm.id ORDER BY dm.ten_danh_muc");
    }

    /**
     * Lấy sản phẩm theo slug danh mục
     * Tương đương GET /:slug/products trong categories.js
     */
    public Map<String, Object> getProductsByCategory(String slug, int page, int limit) {
        int offset = (page - 1) * limit;

        List<Map<String, Object>> categories = db.queryForList(
                "SELECT * FROM DanhMuc WHERE slug = ?", slug);
        if (categories.isEmpty()) return null;

        Map<String, Object> category = categories.get(0);
        Integer categoryId = (Integer) category.get("id");

        List<Map<String, Object>> products = db.queryForList(
                "SELECT sp.*, dm.ten_danh_muc FROM SanPham sp " +
                "LEFT JOIN DanhMuc dm ON sp.danh_muc_id = dm.id " +
                "WHERE sp.danh_muc_id = ? ORDER BY sp.ngay_them DESC LIMIT ? OFFSET ?",
                categoryId, limit, offset);

        Integer total = db.queryForObject(
                "SELECT COUNT(*) FROM SanPham WHERE danh_muc_id = ?", Integer.class, categoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("category", category);
        result.put("products", products);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", total);
        pagination.put("totalPages", (int) Math.ceil((double) total / limit));
        result.put("pagination", pagination);

        return result;
    }
}
