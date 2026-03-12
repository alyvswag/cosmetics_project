package org.example.service.adminStats;

import org.example.constant.TestConstants;
import org.example.model.dao.OrderItems;
import org.example.model.dao.Products;
import org.example.model.dao.ProductInventory;
import org.example.model.dto.enums.order.OrderStatus;
import org.example.model.dto.response.adminStats.OrderStatusResponse;
import org.example.model.dto.response.adminStats.OverviewResponse;
import org.example.model.dto.response.adminStats.TopProductResponse;
import org.example.repo.order.OrderItemRepo;
import org.example.repo.order.OrderRepo;
import org.example.repo.product.ProductInventoryRepo;
import org.example.repo.review.ReviewRepo;
import org.example.repo.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceImplTest {

    @Mock
    OrderRepo orderRepo;
    @Mock
    ReviewRepo reviewRepo;
    @Mock
    UserRepo userRepo;
    @Mock
    OrderItemRepo orderItemRepo;
    @Mock
    ProductInventoryRepo productInventoryRepo;

    @InjectMocks
    AdminStatsServiceImpl adminStatsService;

    @Test
    void getOverview_Success() {
        when(orderRepo.count()).thenReturn(TestConstants.COUNT_VALUE);
        when(orderRepo.sumGrandTotal()).thenReturn(TestConstants.TOTAL_SUM);
        when(userRepo.count()).thenReturn(TestConstants.COUNT_VALUE);

        OverviewResponse response = adminStatsService.getOverview();

        assertNotNull(response);
        assertEquals(TestConstants.COUNT_VALUE, response.getTotalOrders());
        assertEquals(TestConstants.TOTAL_SUM, response.getTotalRevenue());
        assertEquals(TestConstants.COUNT_VALUE, response.getTotalCustomers());
    }

    @Test
    void getTopProducts_Success() {
        Products product = new Products();
        product.setId(TestConstants.PRODUCT_ID);
        product.setName(TestConstants.PRODUCT_NAME);

        OrderItems orderItem = new OrderItems();
        orderItem.setProduct(product);

        ProductInventory inventory = new ProductInventory();
        inventory.setProduct(product);
        inventory.setQuantity(TestConstants.QUANTITY);

        when(orderItemRepo.findAllByProduct_IsActive(true)).thenReturn(List.of(orderItem));
        when(productInventoryRepo.findByIdForProductQuantity(TestConstants.PRODUCT_ID)).thenReturn(Optional.of(inventory));

        List<TopProductResponse> result = adminStatsService.getTopProducts();

        assertFalse(result.isEmpty());
        assertEquals(TestConstants.PRODUCT_NAME, result.get(0).getName());
    }

    @Test
    void getOrderStatusStats_Success() {
        OrderStatus status = OrderStatus.PENDING;
        when(orderRepo.countOrdersByStatus(status)).thenReturn(TestConstants.COUNT_VALUE);

        OrderStatusResponse response = adminStatsService.getOrderStatusStats(status);

        assertEquals(status.name(), response.getStatus());
        assertEquals(TestConstants.COUNT_VALUE, response.getCount());
    }

    @Test
    void getMostReviewedProducts_Success() {
        TopProductResponse mockProduct = new TopProductResponse(TestConstants.PRODUCT_NAME, TestConstants.QUANTITY);
        when(reviewRepo.getMostReviewedProducts()).thenReturn(List.of(mockProduct));

        List<TopProductResponse> result = adminStatsService.getMostReviewedProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TestConstants.PRODUCT_NAME, result.get(0).getName());
    }
}