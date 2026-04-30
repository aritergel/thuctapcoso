package com.shoeshop.dto;

/**
 * DTO cho request cập nhật trạng thái đơn hàng (Admin)
 * Tương đương body của PUT /api/admin/orders/:id/status trong Node.js
 */
public class StatusUpdateRequest {
    private Integer trang_thai;

    public Integer getTrang_thai() { return trang_thai; }
    public void setTrang_thai(Integer trang_thai) { this.trang_thai = trang_thai; }
}
