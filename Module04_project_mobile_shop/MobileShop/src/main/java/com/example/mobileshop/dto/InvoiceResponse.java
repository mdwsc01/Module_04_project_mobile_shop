package com.example.mobileshop.dto;

import com.example.mobileshop.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(Long id, Long customerId, String customerName, LocalDateTime createdAt,
                              InvoiceStatus status, BigDecimal totalAmount, List<Item> items) {
    public record Item(Long productId, String productName, String brand,
                       int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
