package com.example.mobileshop.controller;

import com.example.mobileshop.dto.CustomerRequest;
import com.example.mobileshop.entity.Customer;
import com.example.mobileshop.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Customer> all(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String keyword) {
        if (page < 0 || size < 1 || size > 100)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Phân trang không hợp lệ (size 1–100)");
        return service.search(keyword, page, size);
    }

    @GetMapping("/options")
    public List<Customer> options() {
        return service.all();
    }

    @GetMapping("/{id}")
    public Customer byId(@PathVariable Long id) {
        return service.byId(id);
    }

    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody CustomerRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @Valid @RequestBody CustomerRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
