package com.shoeshop.dto;

/**
 * DTO cho request đăng nhập khách hàng
 * Tương đương body của POST /api/auth/login trong Node.js
 */
public class LoginRequest {
    private String email;
    private String mat_khau;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMat_khau() { return mat_khau; }
    public void setMat_khau(String mat_khau) { this.mat_khau = mat_khau; }
}
