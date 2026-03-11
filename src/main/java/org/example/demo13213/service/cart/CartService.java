package org.example.demo13213.service.cart;

import org.example.demo13213.model.dao.CartItems;
import org.example.demo13213.model.dto.response.cart.ProductCouponResponse;

import java.util.List;

public interface CartService {
    List<CartItems> getUserCart();

    void addCart(Long productId);

    List<ProductCouponResponse> applyCoupon(String couponCode);

    void removeCart(Long productId);
}
