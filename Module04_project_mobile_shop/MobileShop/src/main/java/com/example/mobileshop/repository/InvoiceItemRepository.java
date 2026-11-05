package com.example.mobileshop.repository;

import com.example.mobileshop.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    boolean existsByProductId(Long id);
}
