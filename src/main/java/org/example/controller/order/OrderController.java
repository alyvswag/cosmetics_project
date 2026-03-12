package org.example.controller.order;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dao.OrderItems;
import org.example.model.dao.Orders;
import org.example.model.dto.response.base.BaseResponse;
import org.example.service.order.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Sifarişlərin yaradılması (checkout), ödəniş statuslarının təsdiqi/ləğvi
 istifadəçinin sifariş tarixinə baxılması üçün controller. */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;

    // Səbətdəki məhsulların sifarişə çevrilməsi (ödəniş öncəsi son mərhələ)
    @PostMapping("/checkout")
    public BaseResponse<Orders> checkout() {
        log.info("Order checkout process initiated");
        return BaseResponse.success(orderService.checkoutOrder());
    }

    // Sifarişin ödənişinin uğurla tamamlandığını təsdiq edir
    @PostMapping("/{id}/confirm-payment")
    public BaseResponse<Void> confirmPayment(@PathVariable("id") Long id) {
        log.info("Confirming payment for order ID: {}", id);
        orderService.confirmPayment(id);
        return BaseResponse.success();
    }

    // Ödəniş prosesinin ləğv edilməsi və ya uğursuz olması halı
    @PostMapping("/{id}/cancel-payment")
    public BaseResponse<Void> cancelPayment(@PathVariable("id") Long id) {
        log.warn("Payment cancellation request for order ID: {}", id);
        orderService.cancelPayment(id);
        return BaseResponse.success();
    }

    // İstifadəçinin özünə məxsus keçmiş və cari sifarişlərinin siyahısı
    @PostMapping("/my")
    public BaseResponse<List<OrderItems>> myOrders() {
        log.info("Fetching order history for current user");
        return BaseResponse.success(orderService.myOrders());
    }
}