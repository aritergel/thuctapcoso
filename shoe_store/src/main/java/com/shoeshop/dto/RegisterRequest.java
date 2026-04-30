package com.shoeshop.dto;

/**
 * DTO cho request đăng ký tài khoản
 * Tương đương body của POST /api/auth/register trong Node.js
 */
public class RegisterRequest {
    private String ho_ten;
    private String email;
    private String mat_khau;

    public String getHo_ten() { return ho_ten; }
    public void setHo_ten(String ho_ten) { this.ho_ten = ho_ten; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMat_khau() { return mat_khau; }
    public void setMat_khau(String mat_khau) { this.mat_khau = mat_khau; }
}
