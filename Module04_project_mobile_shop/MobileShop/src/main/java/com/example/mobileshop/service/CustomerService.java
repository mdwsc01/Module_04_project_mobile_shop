package com.example.mobileshop.service;
import com.example.mobileshop.dto.CustomerRequest;
import com.example.mobileshop.entity.Customer;
import com.example.mobileshop.repository.CustomerRepository;
import com.example.mobileshop.repository.InvoiceRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final InvoiceRepository invoices;
    public CustomerService(CustomerRepository customers, InvoiceRepository invoices) {
        this.customers = customers; this.invoices = invoices;
    }
    @Transactional(readOnly = true)
    public List<Customer> all() { return customers.findAllByOrderByIdDesc(); }
    @Transactional(readOnly = true)
    public Page<Customer> search(String keyword, int page, int size) {
        Specification<Customer> spec = (root, query, cb) -> {
            if(keyword == null || keyword.isBlank()) return cb.conjunction();
            String s = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(cb.like(cb.lower(root.get("name")), s), cb.like(cb.lower(root.get("email")), s),
                    cb.like(root.get("phone"), s), cb.like(cb.lower(root.get("address")), s));
        };
        return customers.findAll(spec, PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id")));
    }
    @Transactional(readOnly = true)
    public Customer byId(Long id) { return customers.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy khách hàng")); }
    @Transactional
    public Customer create(CustomerRequest r) {
        if (customers.existsByEmailIgnoreCase(r.email().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email đã tồn tại");
        Customer c = new Customer(); apply(c,r); return customers.save(c);
    }
    @Transactional
    public Customer update(Long id, CustomerRequest r) {
        Customer c = byId(id);
        if (customers.existsByEmailIgnoreCaseAndIdNot(r.email().trim(),id))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email đã tồn tại");
        apply(c,r); return customers.save(c);
    }
    @Transactional
    public void delete(Long id) {
        Customer c = byId(id);
        if (invoices.existsByCustomerId(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Khách hàng đã có hóa đơn; hãy chuyển trạng thái ngừng hoạt động");
        customers.delete(c);
    }
    private void apply(Customer c, CustomerRequest r) {
        c.setName(r.name().trim()); c.setEmail(r.email().trim()); c.setPhone(r.phone().trim());
        c.setAddress(r.address().trim()); c.setActive(r.active()==null || r.active());
    }
}
