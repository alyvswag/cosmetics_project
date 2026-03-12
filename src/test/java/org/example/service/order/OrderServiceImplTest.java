package org.example.service.order;

import org.example.constant.TestConstants;
import org.example.model.dao.*;
import org.example.model.dao.CartItems;
import org.example.model.dao.OrderItems;
import org.example.model.dao.Orders;
import org.example.model.dao.ProductInventory;
import org.example.model.dao.Products;
import org.example.model.dao.Users;
import org.example.model.dto.enums.order.OrderStatus;
import org.example.repo.cart.CartItemRepo;
import org.example.repo.order.OrderItemRepo;
import org.example.repo.order.OrderRepo;
import org.example.repo.product.ProductInventoryRepo;
import org.example.repo.user.UserRepo;
import org.example.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    CartItemRepo cartItemRepo;
    @Mock
    UserRepo userRepo;
    @Mock
    OrderRepo orderRepo;
    @Mock
    OrderItemRepo orderItemsRepo;
    @Mock
    ProductInventoryRepo productInventoryRepo;

    @InjectMocks
    OrderServiceImpl orderService;

    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @Mock
    UserPrincipal userPrincipal;

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userPrincipal);
        lenient().when(userPrincipal.getId()).thenReturn(TestConstants.USER_ID);
        lenient().when(userPrincipal.getUsername()).thenReturn(TestConstants.USERNAME);
    }

    @Test
    void checkoutOrder_Success() {
        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);
        p.setPrice(TestConstants.PRODUCT_PRICE);
        CartItems item = new CartItems();
        item.setProduct(p);
        item.setQuantity(2);
        ProductInventory inv = new ProductInventory();
        inv.setQuantity(TestConstants.STOCK_COUNT);
        Users u = new Users();
        u.setUsername(TestConstants.USERNAME);

        when(cartItemRepo.findByUserIdForCartItem(TestConstants.USER_ID)).thenReturn(List.of(item));
        when(userRepo.findUserByUsername(TestConstants.USERNAME)).thenReturn(Optional.of(u));
        when(productInventoryRepo.findById(TestConstants.PRODUCT_ID)).thenReturn(Optional.of(inv));
        when(orderRepo.save(any(Orders.class))).thenAnswer(i -> i.getArguments()[0]);

        Orders result = orderService.checkoutOrder();

        assertNotNull(result);
        assertEquals(8, inv.getQuantity());
        verify(cartItemRepo).deleteAll(any());
    }

    @Test
    void confirmPayment_Success() {
        Orders order = new Orders();
        when(orderRepo.findByIdForOrders(TestConstants.ORDER_ID)).thenReturn(Optional.of(order));

        orderService.confirmPayment(TestConstants.ORDER_ID);

        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepo).save(order);
    }

    @Test
    void cancelPayment_Success() {
        Orders order = new Orders();
        OrderItems item = new OrderItems();
        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);
        item.setProduct(p);
        item.setQuantity(3);
        ProductInventory inv = new ProductInventory();
        inv.setQuantity(5);

        when(orderRepo.findByIdForOrders(TestConstants.ORDER_ID)).thenReturn(Optional.of(order));
        when(orderItemsRepo.findByOrderItemForOrderId(TestConstants.ORDER_ID)).thenReturn(List.of(item));
        when(productInventoryRepo.findById(TestConstants.PRODUCT_ID)).thenReturn(Optional.of(inv));

        orderService.cancelPayment(TestConstants.ORDER_ID);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(8, inv.getQuantity()); // 5 + 3 = 8
    }

    @Test
    void myOrders_Success() {
        Users u = new Users();
        u.setId(TestConstants.USER_ID);
        Orders order = new Orders();
        order.setId(TestConstants.ORDER_ID);

        when(userRepo.findUserByUsername(TestConstants.USERNAME)).thenReturn(Optional.of(u));
        when(orderRepo.findOrdersByUserId(TestConstants.USER_ID)).thenReturn(List.of(order));
        when(orderItemsRepo.findByOrderItemForOrderId(TestConstants.ORDER_ID)).thenReturn(List.of(new OrderItems()));

        List<OrderItems> result = orderService.myOrders();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}