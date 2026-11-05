package com.example.mobileshop;

import com.example.mobileshop.dto.InvoiceRequest;
import com.example.mobileshop.entity.*;
import com.example.mobileshop.repository.*;
import com.example.mobileshop.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {
    @Mock InvoiceRepository invoices;
    @Mock CustomerRepository customers;
    @Mock ProductRepository products;
    @InjectMocks InvoiceService service;

    @Test void shouldRejectInvoiceWhenStockIsInsufficient() {
        Customer customer = new Customer(); customer.setActive(true);
        Product product = new Product();product.setName("iPhone 16");product.setStock(1);
        product.setPrice(new BigDecimal("15000000"));product.setStatus(ProductStatus.ACTIVE);
        when(customers.findById(1L)).thenReturn(Optional.of(customer));
        when(products.findForUpdate(2L)).thenReturn(Optional.of(product));
        var request = new InvoiceRequest(1L, List.of(new InvoiceRequest.Item(2L,2)));
        ResponseStatusException ex=assertThrows(ResponseStatusException.class,()->service.create(request));
        assertEquals(HttpStatus.CONFLICT,ex.getStatusCode());assertEquals(1,product.getStock());
        verify(invoices,never()).saveAndFlush(any());
    }

    @Test void cancelRestoresStockExactlyOnce() {
        Customer customer=new Customer();ReflectionTestUtils.setField(customer,"id",1L);customer.setName("An");
        Product product=new Product();ReflectionTestUtils.setField(product,"id",2L);
        product.setName("iPhone");product.setBrand("Apple");product.setPrice(new BigDecimal("10000000"));product.setStock(3);
        Invoice invoice=new Invoice();ReflectionTestUtils.setField(invoice,"id",5L);
        invoice.setCustomer(customer);invoice.setTotalAmount(new BigDecimal("20000000"));
        InvoiceItem item=new InvoiceItem();item.setProduct(product);item.setQuantity(2);item.setUnitPrice(product.getPrice());invoice.addItem(item);
        when(invoices.findForUpdate(5L)).thenReturn(Optional.of(invoice));
        when(products.findForUpdate(2L)).thenReturn(Optional.of(product));
        service.changeStatus(5L,InvoiceStatus.CANCELED);
        assertEquals(InvoiceStatus.CANCELED,invoice.getStatus());assertEquals(5,product.getStock());
        service.changeStatus(5L,InvoiceStatus.CANCELED);
        assertEquals(5,product.getStock());verify(products,times(1)).findForUpdate(2L);
    }
}
