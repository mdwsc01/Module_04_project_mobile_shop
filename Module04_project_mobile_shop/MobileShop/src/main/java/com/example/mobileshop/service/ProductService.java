package com.example.mobileshop.service;

import com.example.mobileshop.dto.ProductRequest;
import com.example.mobileshop.entity.Product;
import com.example.mobileshop.entity.ProductStatus;
import com.example.mobileshop.repository.InvoiceItemRepository;
import com.example.mobileshop.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    private final ProductRepository products;
    private final InvoiceItemRepository items;
    public ProductService(ProductRepository products, InvoiceItemRepository items) {
        this.products = products; this.items = items;
    }
    @Transactional(readOnly = true)
    public List<Product> all() { return products.findAllByOrderByIdDesc(); }
    @Transactional(readOnly = true)
    public Page<Product> search(String brand, BigDecimal minPrice, BigDecimal maxPrice,
                                Integer minStock, int page, int size, String sortBy, String direction) {
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0 ||
            minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0 ||
            minStock != null && minStock < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khoảng giá hoặc tồn kho không hợp lệ");
        if (!Set.of("id", "name", "brand", "price", "stock").contains(sortBy))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trường sắp xếp không hợp lệ");
        Sort.Direction d = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(d, sortBy));
        Specification<Product> spec = (root, query, cb) -> {
            var restrictions = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (brand != null && !brand.isBlank())
                restrictions.add(cb.like(cb.lower(root.get("brand")), "%"+ brand.trim().toLowerCase(java.util.Locale.ROOT) +"%"));
            if (minPrice != null) restrictions.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) restrictions.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (minStock != null) restrictions.add(cb.greaterThanOrEqualTo(root.get("stock"), minStock));
            return cb.and(restrictions.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return products.findAll(spec, pageable);
    }
    @Transactional(readOnly = true)
    public Product byId(Long id) { return products.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy sản phẩm")); }
    @Transactional
    public Product create(ProductRequest data) { Product p = new Product(); apply(p,data); return products.save(p); }
    @Transactional
    public Product update(Long id, ProductRequest data) { Product p = byId(id); apply(p,data); return products.save(p); }
    @Transactional
    public void delete(Long id) {
        Product p = byId(id);
        if (items.existsByProductId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Sản phẩm đã có trong hóa đơn; hãy chuyển trạng thái INACTIVE thay vì xóa");
        products.delete(p);
    }
    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim()); p.setBrand(r.brand().trim()); p.setPrice(r.price());
        p.setStock(r.stock()); p.setImageUrl(r.imageUrl());
        p.setStatus(r.status() == null ? ProductStatus.ACTIVE : r.status());
    }
}
