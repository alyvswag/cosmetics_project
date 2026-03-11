package org.example.demo13213.service.product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.model.dao.*;
import org.example.demo13213.model.dto.request.product.ProductFilterRequest;
import org.example.demo13213.model.dto.response.product.ProductResponseDetails;
import org.example.demo13213.repo.coupon.UserCouponRepo;
import org.example.demo13213.repo.order.OrderItemRepo;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.product.ProductRepo;
import org.example.demo13213.repo.review.ReviewRepo;
import org.example.demo13213.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ProductServiceImplTest {

    @Mock
    ProductRepo productRepo;
    @Mock
    ProductInventoryRepo productInventoryRepo;
    @Mock
    ReviewRepo reviewRepo;
    @Mock
    UserCouponRepo userCouponRepo;
    @Mock
    OrderItemRepo orderItemRepo;
    @Mock
    EntityManager em;

    @InjectMocks
    ProductServiceImpl productService;

    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @Mock
    UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userPrincipal);
        lenient().when(userPrincipal.getId()).thenReturn(TestConstants.USER_ID);
    }

    @Test
    void searchProduct_Success() {
        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);
        ProductInventory inv = new ProductInventory();
        inv.setQuantity(5);

        when(productRepo.searchProductsByName(anyString())).thenReturn(new ArrayList<>(List.of(p)));
        when(productInventoryRepo.findById(anyLong())).thenReturn(Optional.of(inv));

        List<Products> result = productService.searchProduct(TestConstants.SEARCH_WORD);
        assertNotNull(result);
    }

    @Test
    void getProductDetails_Success() {
        Categories cat = new Categories();
        cat.setId(1L);
        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);
        p.setPrice(TestConstants.PRODUCT_PRICE);
        p.setCategory(cat);
        ProductInventory inv = new ProductInventory();
        inv.setQuantity(10);

        when(productRepo.findByIdForProduct(anyLong())).thenReturn(Optional.of(p));
        when(productInventoryRepo.findByIdForProductQuantity(anyLong())).thenReturn(Optional.of(inv));
        when(reviewRepo.findByProductIdForReview(anyLong())).thenReturn(new ArrayList<>());
        when(userCouponRepo.findActiveByUserIdForUserCoupon(anyLong())).thenReturn(new ArrayList<>());

        ProductResponseDetails result = productService.getProductDetails(TestConstants.PRODUCT_ID);
        assertNotNull(result);
    }

    @Test
    void getBestSellers_Success() {
        Object[] row = new Object[]{TestConstants.PRODUCT_ID, 100L};

        List<Object[]> mockList = new ArrayList<>();
        mockList.add(row);

        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);

        ProductInventory inv = new ProductInventory();
        inv.setQuantity(TestConstants.STOCK_COUNT);

        when(orderItemRepo.findBestSellingProducts()).thenReturn(mockList);
        when(productRepo.findAllById(any())).thenReturn(new ArrayList<>(List.of(p)));
        when(productInventoryRepo.findById(anyLong())).thenReturn(Optional.of(inv));

        List<Products> result = productService.getBestSellers();

        assertFalse(result.isEmpty());
    }

    @Test
    void filter_Success() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery cq = mock(CriteriaQuery.class);
        Root root = mock(Root.class);
        TypedQuery tq = mock(TypedQuery.class);
        Path pathMock = mock(Path.class);
        Predicate predMock = mock(Predicate.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(any())).thenReturn(cq);
        when(cq.from(any(Class.class))).thenReturn(root);
        when(em.createQuery(any(CriteriaQuery.class))).thenReturn(tq);

        when(root.get(anyString())).thenReturn(pathMock);
        when(cb.isTrue(any())).thenReturn(predMock);
        when(cb.and(any())).thenReturn(predMock);

        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);
        ProductInventory inv = new ProductInventory();
        inv.setQuantity(5);

        when(tq.getResultList()).thenReturn(new ArrayList<>(List.of(p)));
        when(productInventoryRepo.findById(anyLong())).thenReturn(Optional.of(inv));

        ProductFilterRequest req = new ProductFilterRequest();
        req.setMinPrice(TestConstants.MIN_PRICE);

        List<Products> result = productService.filter(req);
        assertNotNull(result);
    }
}