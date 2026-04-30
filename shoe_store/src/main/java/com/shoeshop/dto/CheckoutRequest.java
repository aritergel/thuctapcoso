package com.shoeshop.dto;

/**
 * DTO cho request đặt hàng (checkout)
 * Tương đương body của POST /api/orders/checkout trong Node.js
 */
public class CheckoutRequest {
    private String dia_chi_giao_hang;
    private String ghi_chu;

    public String getDia_chi_giao_hang() { return dia_chi_giao_hang; }
    public void setDia_chi_giao_hang(String dia_chi_giao_hang) { this.dia_chi_giao_hang = dia_chi_giao_hang; }
    public String getGhi_chu() { return ghi_chu; }
    public void setGhi_chu(String ghi_chu) { this.ghi_chu = ghi_chu; }
}
