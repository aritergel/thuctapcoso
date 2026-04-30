-- Tạo database
CREATE DATABASE IF NOT EXISTS shoe_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shoe_store;

-- Bảng Admin
CREATE TABLE IF NOT EXISTS Admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(100),
    email VARCHAR(100),
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng KhachHang
CREATE TABLE IF NOT EXISTS KhachHang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mat_khau VARCHAR(255) NOT NULL,
    sdt VARCHAR(20),
    dia_chi TEXT,
    ngay_dang_ky DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng DanhMuc
CREATE TABLE IF NOT EXISTS DanhMuc (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_danh_muc VARCHAR(100) NOT NULL,
    slug VARCHAR(100),
    mo_ta TEXT
);

-- Bảng SanPham
CREATE TABLE IF NOT EXISTS SanPham (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ten_sp VARCHAR(150) NOT NULL,
    slug VARCHAR(150),
    gia DECIMAL(10,2) NOT NULL,
    gia_km DECIMAL(10,2),
    mo_ta_ngan TEXT,
    mo_ta_chi_tiet LONGTEXT,
    hinh_anh VARCHAR(255),
    size VARCHAR(50),
    so_luong INT DEFAULT 0,
    luot_xem INT DEFAULT 0,
    ngay_them DATETIME DEFAULT CURRENT_TIMESTAMP,
    danh_muc_id INT,
    FOREIGN KEY (danh_muc_id) REFERENCES DanhMuc(id) ON DELETE SET NULL
);

-- Bảng Cart (Giỏ hàng)
CREATE TABLE IF NOT EXISTS Cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    khach_hang_id INT NOT NULL,
    san_pham_id INT NOT NULL,
    so_luong INT DEFAULT 1,
    size VARCHAR(10),
    FOREIGN KEY (khach_hang_id) REFERENCES KhachHang(id) ON DELETE CASCADE,
    FOREIGN KEY (san_pham_id) REFERENCES SanPham(id) ON DELETE CASCADE
);

-- Bảng DonHang
CREATE TABLE IF NOT EXISTS DonHang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ma_don VARCHAR(50) NOT NULL,
    khach_hang_id INT NOT NULL,
    tong_tien DECIMAL(10,2) NOT NULL,
    trang_thai INT DEFAULT 0,
    dia_chi_giao TEXT,
    ghi_chu TEXT,
    ngay_dat DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (khach_hang_id) REFERENCES KhachHang(id)
);

-- Bảng ChiTietDonHang
CREATE TABLE IF NOT EXISTS ChiTietDonHang (
    id INT AUTO_INCREMENT PRIMARY KEY,
    don_hang_id INT NOT NULL,
    san_pham_id INT NOT NULL,
    so_luong INT NOT NULL,
    size VARCHAR(10),
    gia DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (don_hang_id) REFERENCES DonHang(id) ON DELETE CASCADE,
    FOREIGN KEY (san_pham_id) REFERENCES SanPham(id)
);

-- Dữ liệu mẫu Admin (INSERT IGNORE để không lỗi khi restart)
INSERT IGNORE INTO Admin (username, password, ho_ten, email) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', 'admin@shoeshop.com');

-- Dữ liệu mẫu DanhMuc
INSERT IGNORE INTO DanhMuc (id, ten_danh_muc, slug, mo_ta) VALUES
(1, 'Giày Nam', 'giay-nam', 'Các loại giày dành cho nam'),
(2, 'Giày Nữ', 'giay-nu', 'Các loại giày dành cho nữ'),
(3, 'Giày Thể Thao', 'giay-the-thao', 'Giày thể thao đa dạng'),
(4, 'Giày Sneaker', 'giay-sneaker', 'Giày sneaker thời trang'),
(5, 'Giày Cao Gót', 'giay-cao-got', 'Giày cao gót nữ');

-- Dữ liệu mẫu SanPham
INSERT IGNORE INTO SanPham (id, ten_sp, slug, gia, gia_km, mo_ta_ngan, mo_ta_chi_tiet, hinh_anh, size, so_luong, danh_muc_id) VALUES
(1, 'Nike Air Max 270', 'nike-air-max-270', 3500000, 2990000, 'Giày Nike Air Max 270 chính hãng', 'Giày Nike Air Max 270 với đệm khí lớn nhất từ trước đến nay, mang lại sự thoải mái tối đa.', '/uploads/nike-air-max-270.jpg', '39,40,41,42,43', 50, 3),
(2, 'Adidas Ultraboost 22', 'adidas-ultraboost-22', 4200000, 3800000, 'Giày Adidas Ultraboost 22', 'Công nghệ Boost mang lại năng lượng hoàn trả tuyệt vời cho mỗi bước chạy.', '/uploads/adidas-ultraboost.jpg', '39,40,41,42,43,44', 35, 3),
(3, 'Converse Chuck Taylor', 'converse-chuck-taylor', 1500000, NULL, 'Giày Converse Classic', 'Giày Converse Chuck Taylor All Star - biểu tượng thời trang vượt thời gian.', '/uploads/converse-chuck.jpg', '36,37,38,39,40,41,42', 100, 4),
(4, 'Vans Old Skool', 'vans-old-skool', 1800000, 1600000, 'Giày Vans Old Skool', 'Thiết kế cổ điển với đường sọc Jazz Stripe đặc trưng.', '/uploads/vans-oldskool.jpg', '36,37,38,39,40,41,42,43', 80, 4),
(5, 'Puma RS-X', 'puma-rs-x', 2800000, 2500000, 'Giày Puma RS-X', 'Phong cách retro-futuristic với công nghệ Running System.', '/uploads/puma-rsx.jpg', '39,40,41,42,43', 45, 3),
(6, 'New Balance 574', 'new-balance-574', 2200000, NULL, 'Giày New Balance 574', 'Thiết kế classic với sự thoải mái hàng ngày.', '/uploads/nb-574.jpg', '38,39,40,41,42,43,44', 60, 1),
(7, 'Giày Cao Gót Đen', 'giay-cao-got-den', 890000, 750000, 'Giày cao gót đen thanh lịch', 'Giày cao gót 7cm, chất liệu da cao cấp, phù hợp công sở.', '/uploads/cao-got-den.jpg', '35,36,37,38,39', 40, 5),
(8, 'Nike Air Force 1', 'nike-air-force-1', 2800000, 2500000, 'Nike Air Force 1 White', 'Biểu tượng sneaker với thiết kế trắng tinh khiết.', '/uploads/nike-af1.jpg', '38,39,40,41,42,43,44', 70, 4);
