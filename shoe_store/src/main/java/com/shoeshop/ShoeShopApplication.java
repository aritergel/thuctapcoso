package com.shoeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point cho ứng dụng ShoeShop - Web Bán Giày
 * Tương đương với server.js trong phiên bản Node.js
 */
@SpringBootApplication
public class ShoeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoeShopApplication.class, args);
    }
}
