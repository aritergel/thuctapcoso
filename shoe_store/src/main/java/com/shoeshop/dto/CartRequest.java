package com.shoeshop.dto;

/**
 * DTO cho request thêm/cập nhật giỏ hàng
 */
public class CartRequest {
    private Integer san_pham_id;
    private Integer so_luong = 1;
    private String size;
    // Dùng cho endpoint /api/cart/update (cart.html)
    private Integer product_id;
    private Integer delta;

    public Integer getSan_pham_id() { return san_pham_id; }
    public void setSan_pham_id(Integer san_pham_id) { this.san_pham_id = san_pham_id; }
    public Integer getSo_luong() { return so_luong; }
    public void setSo_luong(Integer so_luong) { this.so_luong = so_luong; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public Integer getProduct_id() { return product_id; }
    public void setProduct_id(Integer product_id) { this.product_id = product_id; }
    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
}
