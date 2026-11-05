package com.example.mobileshop.controller;

import com.example.mobileshop.entity.Invoice;
import com.example.mobileshop.entity.InvoiceStatus;
import com.example.mobileshop.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final InvoiceRepository invoices;

    public DashboardController(CustomerRepository customers, ProductRepository products, InvoiceRepository invoices) {
        this.customers = customers;
        this.products = products;
        this.invoices = invoices;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        var completed = invoices.findByStatus(InvoiceStatus.COMPLETED);
        var revenue = completed.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("customers", customers.count(), "products", products.count(),
                "invoices", invoices.count(), "revenue", revenue);
    }

    // Revenue recognized when an invoice is COMPLETED (completedAt), not for pending/canceled orders.
    // Older completed invoices without completedAt use createdAt for compatibility.
    @GetMapping("/revenue")
    @Transactional(readOnly = true)
    public Map<String, Object> revenue(@RequestParam(defaultValue = "month") String groupBy) {
        if (!Set.of("day", "month", "year").contains(groupBy))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "groupBy phải là day, month hoặc year");
        Map<String, BigDecimal> grouped = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(switch (groupBy) {
            case "day" -> "yyyy-MM-dd";
            case "month" -> "yyyy-MM";
            default -> "yyyy";
        });
        for (Invoice invoice : invoices.findByStatus(InvoiceStatus.COMPLETED)) {
            LocalDateTime at = invoice.getCompletedAt() != null ? invoice.getCompletedAt() : invoice.getCreatedAt();
            grouped.merge(at.format(fmt), invoice.getTotalAmount(), BigDecimal::add);
        }
        var series = grouped.entrySet().stream().map(e -> Map.of("period", e.getKey(), "revenue", e.getValue())).toList();
        return Map.of("groupBy", groupBy, "series", series,
                "totalRevenue", grouped.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
