package com.shoeshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controller serve các trang HTML tĩnh
 * Tương đương các app.get('/', ...) trong server.js
 *
 * Spring Boot tự động serve file trong resources/static/
 * Controller này chỉ cần cho các route đặc biệt (như /product/:slug)
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }

    // Route động: /product/nike-air-max-270 → serve product.html
    @GetMapping("/product/{slug}")
    public String productDetail(@PathVariable String slug) {
        return "forward:/product.html";
    }

    @GetMapping("/products")
    public String products() {
        return "forward:/products.html";
    }

    @GetMapping("/cart")
    public String cart() {
        return "forward:/cart.html";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "forward:/checkout.html";
    }

    @GetMapping("/orders")
    public String orders() {
        return "forward:/orders.html";
    }

    @GetMapping("/account")
    public String account() {
        return "forward:/account.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "forward:/admin/login.html";
    }
}
