package com.example.mobileshop.service;

import com.example.mobileshop.dto.InvoiceRequest;
import com.example.mobileshop.dto.InvoiceResponse;
import com.example.mobileshop.entity.*;
import com.example.mobileshop.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class InvoiceService {
    private final InvoiceRepository invoices;
    private final CustomerRepository customers;
    private final ProductRepository products;
    public InvoiceService(InvoiceRepository invoices, CustomerRepository customers, ProductRepository products) {
        this.invoices = invoices; this.customers = customers; this.products = products;
    }
    @Transactional(readOnly = true)
    public List<InvoiceResponse> all() { return invoices.findAllByOrderByIdDesc().stream().map(this::response).toList(); }
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> search(String customer, LocalDate date, int page, int size) {
        Specification<Invoice> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (customer != null && !customer.isBlank())
                predicates.add(cb.like(cb.lower(root.get("customer").get("name")),
                    "%" + customer.trim().toLowerCase(Locale.ROOT) + "%"));
            if (date != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), date.atStartOfDay()));
                predicates.add(cb.lessThan(root.get("createdAt"), date.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return invoices.findAll(spec,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"))).map(this::response);
    }
    @Transactional(readOnly = true)
    public InvoiceResponse byId(Long id) { return response(find(id)); }
    private Invoice find(Long id) { return invoices.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy hóa đơn")); }

    @Transactional
    public InvoiceResponse create(InvoiceRequest r) {
        Customer c = customers.findById(r.customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy khách hàng"));
        if (!c.isActive()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Khách hàng không hoạt động");
        // Group duplicate product IDs before taking row locks (stable lock order).
        Map<Long,Integer> quantities = new TreeMap<>();
        for (InvoiceRequest.Item line : r.items()) {
            if (line.quantity() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Số lượng phải >= 1");
            quantities.merge(line.productId(),line.quantity(), Math::addExact);
        }
        Invoice invoice = new Invoice(); invoice.setCustomer(c);
        BigDecimal sum = BigDecimal.ZERO;
        for (var entry : quantities.entrySet()) {
            Product p = products.findForUpdate(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Sản phẩm không tồn tại: " + entry.getKey()));
            int qty = entry.getValue();
            if (p.getStatus() != ProductStatus.ACTIVE) throw new ResponseStatusException(HttpStatus.CONFLICT,"Sản phẩm đã ngừng bán: " + p.getName());
            if (p.getStock() < qty) throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không đủ tồn kho cho " + p.getName() + " (còn " + p.getStock() + ")");
            p.setStock(p.getStock() - qty);
            InvoiceItem item = new InvoiceItem(); item.setProduct(p); item.setQuantity(qty); item.setUnitPrice(p.getPrice());
            invoice.addItem(item);
            sum = sum.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        invoice.setTotalAmount(sum);
        return response(invoices.saveAndFlush(invoice));
    }

    @Transactional
    public InvoiceResponse changeStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoices.findForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy hóa đơn"));
        InvoiceStatus old = invoice.getStatus();
        if (old == status) return response(invoice);
        if (old == InvoiceStatus.CANCELED || (old == InvoiceStatus.COMPLETED && status != InvoiceStatus.CANCELED))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Hóa đơn đã kết thúc, không thể đổi trạng thái");
        if (status == InvoiceStatus.CANCELED) {
            // Restore inventory once; invoice stays in history.
            List<InvoiceItem> items = invoice.getItems().stream()
                    .sorted(Comparator.comparing(it -> it.getProduct().getId())).toList();
            for (InvoiceItem item : items) {
                Product product = products.findForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"Sản phẩm bị thiếu"));
                product.setStock(product.getStock() + item.getQuantity());
            }
        } else if (!allowedNext(old).contains(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trạng thái không hợp lệ: " + old + " → " + status);
        }
        invoice.setStatus(status);
        if(status == InvoiceStatus.CANCELED) invoice.setCompletedAt(null);
        if(status == InvoiceStatus.COMPLETED) invoice.setCompletedAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
        return response(invoice);
    }
    private Set<InvoiceStatus> allowedNext(InvoiceStatus old) {
        return switch(old) {
            case PENDING -> Set.of(InvoiceStatus.CONFIRMING);
            case CONFIRMING -> Set.of(InvoiceStatus.SHIPPING);
            case SHIPPING -> Set.of(InvoiceStatus.COMPLETED);
            default -> Set.of();
        };
    }
    private InvoiceResponse response(Invoice i) {
        return new InvoiceResponse(i.getId(),i.getCustomer().getId(),i.getCustomer().getName(),
                i.getCreatedAt(),i.getStatus(),i.getTotalAmount(),i.getItems().stream()
                .map(it -> new InvoiceResponse.Item(it.getProduct().getId(),it.getProduct().getName(),
                        it.getProduct().getBrand(),it.getQuantity(),it.getUnitPrice(),
                        it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())))).toList());
    }
}
