package org.example.service.cart;

import org.example.model.dao.CartItems;
import org.example.model.dto.response.cart.ProductCouponResponse;

import java.util.List;

public interface CartService {
    List<CartItems> getUserCart();

    void addCart(Long productId);

    List<ProductCouponResponse> applyCoupon(String couponCode);

    void removeCart(Long productId);
}
