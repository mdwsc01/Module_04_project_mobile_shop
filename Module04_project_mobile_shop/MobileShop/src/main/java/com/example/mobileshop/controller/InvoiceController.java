package com.example.mobileshop.controller;

import com.example.mobileshop.dto.*;
import com.example.mobileshop.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping
    public Page<InvoiceResponse> all(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String customer,
                                     @RequestParam(required = false) LocalDate date) {
        if (page < 0 || size < 1 || size > 100) throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Phân trang không hợp lệ (size 1–100)");
        return service.search(customer, date, page, size);
    }

    @GetMapping("/{id}")
    public InvoiceResponse byId(@PathVariable Long id) {
        return service.byId(id);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PatchMapping("/{id}/status")
    public InvoiceResponse status(@PathVariable Long id,
                                  @Valid @RequestBody InvoiceStatusRequest r) {
        return service.changeStatus(id, r.status());
    }
}
