package org.example.controller.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dao.CartItems;
import org.example.model.dto.response.base.BaseResponse;
import org.example.model.dto.response.cart.ProductCouponResponse;
import org.example.service.cart.CartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* İstifadəçinin səbət əməliyyatlarını (məhsul əlavə etmə, silmə, kupon tətbiqi)
 idarə edən controller. */
@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Cari istifadəçinin səbətindəki bütün məhsulları gətirir
    @GetMapping("/")
    public BaseResponse<List<CartItems>> getCart() {
        log.info("Fetching shopping cart items for current user");
        return BaseResponse.success(cartService.getUserCart());
    }

    // Səbətə yeni məhsul əlavə edilməsi
    @PostMapping("/items/")
    public BaseResponse<Void> addCart(@RequestParam Long productId) {
        log.info("Request to add product ID: {} to cart", productId);
        cartService.addCart(productId);
        return BaseResponse.success();
    }

    // Səbətdəki məhsullara endirim kuponunun tətbiq edilməsi
    @PostMapping("/apply-coupon/")
    public BaseResponse<List<ProductCouponResponse>> addCart(@RequestParam String coupon) {
        log.info("Applying discount coupon: [{}] to cart", coupon);
        return BaseResponse.success(cartService.applyCoupon(coupon));
    }

    // Səbətdən konkret bir elementin (item) silinməsi
    @DeleteMapping("/delete-card/{id}")
    public BaseResponse<Void> deleteCart(@PathVariable Long id) {
        log.info("Removing item ID: {} from shopping cart", id);
        cartService.removeCart(id);
        return BaseResponse.success();
    }
}