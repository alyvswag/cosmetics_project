package org.example.demo13213.service.order;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.exception.BaseException;
import org.example.demo13213.model.dao.*;
import org.example.demo13213.model.dto.enums.order.OrderStatus;
import org.example.demo13213.repo.cart.CartItemRepo;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.order.OrderItemRepo;
import org.example.demo13213.repo.order.OrderRepo;
import org.example.demo13213.repo.user.UserRepo;
import org.example.demo13213.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.example.demo13213.model.dto.enums.order.OrderStatus.CANCELLED;
import static org.example.demo13213.model.dto.enums.order.OrderStatus.PAID;
import static org.example.demo13213.model.dto.enums.response.ErrorResponseMessages.*;

/* Sifarişlərin yaradılması, ödəniş təsdiqi və ləğv edilməsi kimi
 mürəkkəb biznes məntiqlərini idarə edən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    final CartItemRepo cartItemRepo;
    final UserRepo userRepo;
    final OrderRepo orderRepo;
    final OrderItemRepo orderItemsRepo;
    final ProductInventoryRepo productInventoryRepo;

    // Səbətdəki məhsulları sifarişə çevirir, stoku azaldır və yekun məbləği hesablayır
    @Override
    @Transactional
    public Orders checkoutOrder() {
        // 1. Aktiv sessiyadakı istifadəçi məlumatlarını əldə edirik
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // 2. İstifadəçinin səbətinin dolu olub-olmadığını yoxlayırıq
        List<CartItems> cartItems = cartItemRepo.findByUserIdForCartItem(user.getId());
        if (cartItems.isEmpty()) {
            log.warn("Checkout failed: Cart is empty for user ID: {}", user.getId());
            throw BaseException.of(CART_EMPTY);
        }

        // 3. İstifadəçi entity-sini bazadan tapırıq
        Users u = userRepo.findUserByUsername(user.getUsername())
                .orElseThrow(() -> {
                    log.error("User context inconsistency: Username {} not found", user.getUsername());
                    return BaseException.notFound(Users.class.getSimpleName(), "username", user.getUsername());
                });

        // 4. İlkin sifariş (Order) qeydini PENDING statusu ilə yaradırıq
        Orders order = new Orders();
        order.setUser(u);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingFee(BigDecimal.valueOf(5)); // Standart çatdırılma haqqı: 5 AZN
        orderRepo.save(order);

        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        // 5. Səbətdəki hər bir məhsul üçün sifariş detalı (OrderItem) yaradırıq
        for (CartItems cartItem : cartItems) {
            Products product = cartItem.getProduct();
            Integer requestedQty = cartItem.getQuantity();

            // 5.1. Stok vəziyyətini yoxlayırıq
            ProductInventory inventory = productInventoryRepo.findById(product.getId())
                    .orElseThrow(() -> {
                        log.error("Inventory record missing for product ID: {}", product.getId());
                        return BaseException.notFound(ProductInventory.class.getSimpleName(),
                                "productId", String.valueOf(product.getId()));
                    });

            if (inventory.getQuantity() < requestedQty) {
                log.warn("Insufficient stock for product ID: {}. Requested: {}, Available: {}",
                        product.getId(), requestedQty, inventory.getQuantity());
                throw BaseException.of(PRODUCT_OUT_OF_STOCK);
            }

            // 5.2. Məbləğ hesablamaları (Vahid qiymət * Say)
            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));

            // 5.3. OrderItem qeydini yadda saxlayırıq
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQty);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);
            orderItemsRepo.save(orderItem);

            // 5.4. Ümumi cəmləri toplayırıq
            subtotal = subtotal.add(lineTotal);
            totalItems += requestedQty;

            // 5.5. Stokdan müvafiq miqdarı azaldırıq
            inventory.setQuantity(inventory.getQuantity() - requestedQty);
            productInventoryRepo.save(inventory);
        }

        // 6. Sifarişin yekun maliyyə məlumatlarını set edirik
        order.setTotalItems(totalItems);
        order.setSubtotal(subtotal);
        order.setDiscountTotal(BigDecimal.ZERO);

        BigDecimal grandTotal = subtotal
                .subtract(order.getDiscountTotal())
                .add(order.getShippingFee());
        order.setGrandTotal(grandTotal);
        orderRepo.save(order);

        // 7. Sifariş tamamlandığı üçün səbəti təmizləyirik
        cartItemRepo.deleteAll(cartItems);

        log.info("Order successfully created. Order ID: {}, Grand Total: {}", order.getId(), grandTotal);
        return order;
    }

    // Ödənişin uğurla tamamlandığını təsdiq edir
    @Override
    public void confirmPayment(Long id) {
        log.info("Confirming payment for order ID: {}", id);
        Orders order = orderRepo.findByIdForOrders(id)
                .orElseThrow(() -> {
                    log.error("Order not found for confirmation. ID: {}", id);
                    return BaseException.notFound("orders", id.toString(), id);
                });
        order.setStatus(PAID);
        orderRepo.save(order);
    }

    // Ödəniş ləğv edildikdə statusu yeniləyir və məhsulları stoka geri qaytarır
    @Override
    @Transactional
    public void cancelPayment(Long id) {
        log.info("Initiating cancellation for order ID: {}", id);
        Orders order = orderRepo.findByIdForOrders(id)
                .orElseThrow(() -> {
                    log.error("Order not found for cancellation. ID: {}", id);
                    return BaseException.notFound("orders", id.toString(), id);
                });

        order.setStatus(CANCELLED);

        // Sifarişə aid məhsulları tapıb stoka geri yükləyirik
        List<OrderItems> orderItems = orderItemsRepo.findByOrderItemForOrderId(id);
        if (orderItems.isEmpty()) {
            log.warn("Cancellation notice: No items found for order ID: {}", id);
            throw BaseException.of(ORDER_EMPTY);
        }

        for (OrderItems orderItem : orderItems) {
            ProductInventory inventory = productInventoryRepo.findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> BaseException.notFound(ProductInventory.class.getSimpleName(),
                            "productId", String.valueOf(orderItem.getProduct().getId())));

            inventory.setQuantity(inventory.getQuantity() + orderItem.getQuantity());
            productInventoryRepo.save(inventory);
        }

        orderRepo.save(order);
        log.info("Order ID: {} successfully cancelled and inventory restocked", id);
    }

    // İstifadəçinin bütün keçmiş sifarişlərini və detallarını gətirir
    @Override
    public List<OrderItems> myOrders() {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        log.info("Fetching order history for user: {}", user.getUsername());

        Users u = userRepo.findUserByUsername(user.getUsername())
                .orElseThrow(() -> BaseException.notFound(Users.class.getSimpleName(), "username", user.getUsername()));

        List<Orders> orders = orderRepo.findOrdersByUserId(u.getId());
        List<OrderItems> allOrderItems = new ArrayList<>();

        for (Orders order : orders) {
            List<OrderItems> items = orderItemsRepo.findByOrderItemForOrderId(order.getId());
            allOrderItems.addAll(items);
        }
        return allOrderItems;
    }
}