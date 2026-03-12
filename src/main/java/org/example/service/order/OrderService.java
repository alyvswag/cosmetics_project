package org.example.service.order;

import org.example.model.dao.OrderItems;
import org.example.model.dao.Orders;

import java.util.List;

public interface OrderService {
    Orders checkoutOrder();

    void confirmPayment(Long id);

    void cancelPayment(Long id);

    List<OrderItems> myOrders();
}