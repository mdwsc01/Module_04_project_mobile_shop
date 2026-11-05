package com.example.mobileshop;
import com.example.mobileshop.repository.*;
import com.example.mobileshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
 @Mock ProductRepository products;
 @Mock InvoiceItemRepository items;
 @Test void rejectsMinPriceAboveMax(){
    var svc=new ProductService(products,items);
    assertThrows(ResponseStatusException.class,()->svc.search("Apple",new BigDecimal("500"),new BigDecimal("100"),0,0,10,"id","desc"));
    verifyNoInteractions(products);
 }
 @Test void supportsPaginationAndPriceSort(){
    when(products.findAll(any(Specification.class),any(Pageable.class))).thenReturn(Page.empty());
    var svc=new ProductService(products,items);
    svc.search("Sam",BigDecimal.ZERO,new BigDecimal("20000000"),1,1,5,"price","asc");
    var pageable=org.mockito.ArgumentCaptor.forClass(Pageable.class);
    verify(products).findAll(any(Specification.class),pageable.capture());
    assertEquals(1,pageable.getValue().getPageNumber());
    assertEquals(5,pageable.getValue().getPageSize());
    assertEquals(Sort.Direction.ASC,pageable.getValue().getSort().getOrderFor("price").getDirection());
 }
}
