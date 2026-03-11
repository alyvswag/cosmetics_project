package org.example.demo13213.service.cart;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.model.dao.*;
import org.example.demo13213.model.dto.response.cart.ProductCouponResponse;
import org.example.demo13213.repo.cart.CartItemRepo;
import org.example.demo13213.repo.cart.CartRepo;
import org.example.demo13213.repo.coupon.CouponRepo;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.product.ProductRepo;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    CartRepo cartRepo;
    @Mock
    CartItemRepo cartItemRepo;
    @Mock
    ProductRepo productRepo;
    @Mock
    CouponRepo couponRepo;
    @Mock
    ProductInventoryRepo productInventoryRepo;

    @InjectMocks
    CartServiceImpl cartService;

    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @Mock
    UserPrincipal userPrincipal;

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(TestConstants.USER_ID);
    }

    @Test
    void getUserCart_Success() {
        when(cartItemRepo.findByUserIdForCartItem(TestConstants.USER_ID)).thenReturn(List.of(new CartItems()));

        List<CartItems> result = cartService.getUserCart();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void addCart_Success() {
        Products product = new Products();
        product.setId(TestConstants.PRODUCT_ID);
        ProductInventory inventory = new ProductInventory();
        inventory.setQuantity(10);
        Carts cart = new Carts();
        cart.setId(10L);

        when(productRepo.findByIdForProduct(TestConstants.PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productInventoryRepo.findById(product.getId())).thenReturn(Optional.of(inventory));
        when(cartRepo.findByUserIdForCarts(TestConstants.USER_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.addCart(TestConstants.PRODUCT_ID));
        verify(cartItemRepo, times(1)).save(any(CartItems.class));
    }

    @Test
    void applyCoupon_Success() {
        Categories category = new Categories();
        category.setId(1L);

        Coupons coupon = new Coupons();
        coupon.setDiscountValue(TestConstants.DISCOUNT_VALUE);
        coupon.setCategory(category);
        coupon.setIsActive(true);

        Products product = new Products();
        product.setId(TestConstants.PRODUCT_ID);
        product.setPrice(new BigDecimal("100.00"));
        product.setCategory(category);

        CartItems item = new CartItems();
        item.setProduct(product);

        when(couponRepo.findCoupons(TestConstants.COUPON_CODE, true)).thenReturn(Optional.of(coupon));
        when(cartItemRepo.findByUserIdForCartItem(TestConstants.USER_ID)).thenReturn(List.of(item));

        List<ProductCouponResponse> result = cartService.applyCoupon(TestConstants.COUPON_CODE);

        assertFalse(result.isEmpty());
        assertEquals(new BigDecimal("80.00"), result.get(0).getFinalPrice());
    }

    @Test
    void removeCart_Success() {
        assertDoesNotThrow(() -> cartService.removeCart(1L));
        verify(cartRepo, times(1)).deleteById(1L);
    }
}