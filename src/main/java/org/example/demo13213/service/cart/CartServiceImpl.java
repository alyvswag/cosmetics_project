package org.example.demo13213.service.cart;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.exception.BaseException;
import org.example.demo13213.model.dao.*;
import org.example.demo13213.model.dto.response.cart.ProductCouponResponse;
import org.example.demo13213.repo.cart.CartItemRepo;
import org.example.demo13213.repo.cart.CartRepo;
import org.example.demo13213.repo.coupon.CouponRepo;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.product.ProductRepo;
import org.example.demo13213.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.example.demo13213.model.dto.enums.response.ErrorResponseMessages.COUPON_NOT_APPLICABLE;
import static org.example.demo13213.model.dto.enums.response.ErrorResponseMessages.PRODUCT_OUT_OF_STOCK;

/* İstifadəçinin səbət əməliyyatlarını (məhsul əlavə etmə, silmə, kupon tətbiqi)
 həyata keçirən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    final CartRepo cartRepo;
    final CartItemRepo cartItemRepo;
    final ProductRepo productRepo;
    final CouponRepo couponRepo;
    final ProductInventoryRepo productInventoryRepo;

    // Cari autentifikasiya olunmuş istifadəçinin səbətindəki məhsulları gətirir
    @Override
    public List<CartItems> getUserCart() {
        // 1. Təhlükəsizlik kontekstindən cari istifadəçinin identifikasiyası
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        log.info("Requesting cart items for user ID: {}", user.getId());

        // 2. İstifadəçiyə aid səbət elementlərinin bazadan çəkilməsi
        List<CartItems> items = cartItemRepo.findByUserIdForCartItem(user.getId());
        log.debug("Successfully retrieved {} items from cart for user {}", items.size(), user.getId());

        return items;
    }

    // Səbətə məhsul əlavə edir (stok və səbət mövcudluğunu yoxlayaraq)
    @Override
    public void addCart(Long productId) {
        log.info("Initiating process to add product ID: {} to shopping cart", productId);

        // 1. Məhsulun sistemdə mövcudluğunun yoxlanılması
        Products product = productRepo.findByIdForProduct(productId).orElseThrow(() -> {
            log.error("Add to cart failed: Product ID {} not found", productId);
            return BaseException.notFound("product", productId.toString(), productId);
        });

        // 2. Məhsulun anbarda (inventory) mövcudluğunun yoxlanılması
        ProductInventory inventory = productInventoryRepo.findById(product.getId())
                .orElseThrow(() -> {
                    log.error("Data Integrity Failure: Inventory record missing for product ID: {}", product.getId());
                    return BaseException.notFound(ProductInventory.class.getSimpleName(),
                            "productId", String.valueOf(product.getId()));
                });

        // 3. Stok miqdarının kifayət qədər olub-olmadığının təsdiqi
        if (inventory.getQuantity() <= 0) {
            log.warn("Inventory check failed: Product ID {} is currently out of stock", productId);
            throw BaseException.of(PRODUCT_OUT_OF_STOCK);
        }

        // 4. Məhsulu əlavə edəcək istifadəçinin və onun səbətinin müəyyənləşdirilməsi
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Carts cart = cartRepo.findByUserIdForCarts(user.getId())
                .orElseThrow(() -> {
                    log.error("Cart retrieval failed: No active cart found for user ID: {}", user.getId());
                    return BaseException.notFound("cart", user.getId().toString(), "user");
                });

        // 5. Yeni səbət elementinin (CartItem) yaradılması və persist edilməsi
        CartItems ci = new CartItems();
        ci.setProduct(product);
        ci.setCart(cart);
        cartItemRepo.save(ci);

        log.info("Successfully added product [{}] to cart ID: {} for user: {}",
                product.getName(), cart.getId(), user.getUsername());
    }

    // Səbətdəki uyğun məhsullara endirim kuponu tətbiq edir
    @Override
    public List<ProductCouponResponse> applyCoupon(String couponCode) {
        log.info("Attempting to apply coupon code: [{}] to cart items", couponCode);

        // 1. Kuponun aktivliyinin və mövcudluğunun yoxlanılması
        Coupons coupon = couponRepo.findCoupons(couponCode, true)
                .orElseThrow(() -> {
                    log.error("Coupon validation failed: [{}] is either invalid or expired", couponCode);
                    return BaseException.notFound("coupon", couponCode, "coupon");
                });

        // 2. Səbətdəki məhsulların siyahısının götürülməsi
        List<CartItems> cartItems = getUserCart();
        Long couponCategoryId = coupon.getCategory() != null ? coupon.getCategory().getId() : null;

        log.debug("Evaluating coupon applicability for {} cart items based on category ID: {}",
                cartItems.size(), couponCategoryId);

        // 3. Kuponun şərtlərinə (məs. kategoriya) uyğun məhsulların filterlənməsi və qiymət hesablama
        List<ProductCouponResponse> result = cartItems.stream()
                .filter(ci -> {
                    // Əgər kuponun kateqoriya məhdudiyyəti yoxdursa bütün məhsullara aiddir
                    if (couponCategoryId == null) return true;
                    return ci.getProduct() != null
                            && ci.getProduct().getCategory() != null
                            && Objects.equals(ci.getProduct().getCategory().getId(), couponCategoryId);
                })
                .map(ci -> {
                    Products p = ci.getProduct();
                    BigDecimal price = p.getPrice();
                    BigDecimal discount = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;

                    // Endirimli qiymətin hesablanması (Mənfiyə düşməmək şərti ilə)
                    BigDecimal finalPrice = price.subtract(discount);
                    if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                        finalPrice = BigDecimal.ZERO;
                    }

                    return ProductCouponResponse.builder()
                            .p(p)
                            .discValue(discount)
                            .finalPrice(finalPrice)
                            .build();
                })
                .toList();

        // 4. Əgər heç bir məhsul kupon şərtlərinə uymursa xəta qaytarılır
        if (result.isEmpty()) {
            log.warn("Coupon application rejected: No items in cart match category requirements for [{}]", couponCode);
            throw BaseException.of(COUPON_NOT_APPLICABLE);
        }

        log.info("Coupon [{}] successfully applied to {} items", couponCode, result.size());
        return result;
    }

    // Səbətdən məhsulu (və ya səbət elementini) silir
    @Override
    public void removeCart(Long productId) {
        log.info("Request to remove item from cart. Target ID: {}", productId);

        // Səbət elementinin (CartItem) identifikatoruna görə silinməsi
        cartRepo.deleteById(productId);

        log.info("Item ID: {} successfully removed from the database", productId);
    }
}