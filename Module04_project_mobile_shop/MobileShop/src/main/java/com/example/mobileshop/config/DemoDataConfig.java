package com.example.mobileshop.config;

import com.example.mobileshop.entity.*;
import com.example.mobileshop.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner seed(ProductRepository products, CustomerRepository customers,
                           @Value("${app.seed-demo-data:false}") boolean enabled) {
        return args -> {
            if (!enabled) return;
            if (products.count() == 0) {
                addProduct(products, "iPhone 16 Pro 256GB", "Apple", "28990000", 12);
                addProduct(products, "Galaxy S25 Ultra 256GB", "Samsung", "27990000", 9);
                addProduct(products, "Xiaomi 15 256GB", "Xiaomi", "17990000", 18);
                addProduct(products, "OPPO Reno13 5G", "OPPO", "12990000", 15);
                addProduct(products, "vivo V50 5G", "vivo", "11990000", 11);
            }
            if (customers.count() == 0) {
                addCustomer(customers, "Nguyễn Văn An", "an.demo@example.com", "0987654321", "Hà Nội");
                addCustomer(customers, "Trần Thị Bình", "binh.demo@example.com", "0912345678", "Hải Phòng");
                addCustomer(customers, "Lê Minh Châu", "chau.demo@example.com", "0977778888", "Đà Nẵng");
            }
        };
    }

    private static void addProduct(ProductRepository r, String name, String brand, String price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setStatus(ProductStatus.ACTIVE);
        r.save(p);
    }

    private static void addCustomer(CustomerRepository r, String name, String email, String phone, String address) {
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        r.save(c);
    }
}
