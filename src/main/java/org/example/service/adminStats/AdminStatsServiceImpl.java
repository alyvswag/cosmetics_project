package org.example.service.adminStats;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dao.OrderItems;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/* Admin panelində göstərilən bütün statistik hesabatların (ümumi gəlir, sifariş sayı,
 top məhsullar və s.) biznes məntiqini icra edən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class AdminStatsServiceImpl implements AdminStatsService {

    final OrderRepo orderRepo;
    final ReviewRepo reviewRepo;
    final UserRepo userRepo;
    final OrderItemRepo orderItemRepo;
    final ProductInventoryRepo productInventoryRepo;

    // Sistemdəki ümumi sifariş sayını, cəmi gəliri və qeydiyyatdan keçmiş istifadəçi sayını hesablayır
    @Override
    public OverviewResponse getOverview() {
        log.info("Calculating general admin dashboard overview statistics");

        // 1. Ümumi sifarişlərin sayını götürürük
        long ordersCount = orderRepo.count();

        // 2. Sistemdəki bütün tamamlanmış satışların cəmini hesablayırıq
        BigDecimal grandTotalSum = orderRepo.sumGrandTotal();

        // 3. Qeydiyyatdan keçmiş cəmi istifadəçi sayını əldə edirik
        long usersCount = userRepo.count();

        return new OverviewResponse(ordersCount, grandTotalSum, usersCount);
    }

    // Hal-hazırda aktiv olan və satışı davam edən ən populyar məhsulların siyahısını hazırlayır
    @Override
    public List<TopProductResponse> getTopProducts() {
        log.info("Generating top selling products list based on active inventory");

        // 1. Aktiv statuslu bütün sifariş elementlərini (satılmış məhsulları) çəkirik
        List<OrderItems> oi = orderItemRepo.findAllByProduct_IsActive(Boolean.TRUE);
        List<TopProductResponse> list = new ArrayList<>();

        // 2. Hər bir satılmış məhsul üçün anbar qalığını yoxlayıb siyahıya əlavə edirik
        for (OrderItems o : oi) {
            ProductInventory p = productInventoryRepo.findByIdForProductQuantity(o.getProduct().getId()).orElseThrow(
            );
            list.add(new TopProductResponse(p.getProduct().getName(), p.getQuantity()));
        }

        return list;
    }

    // Müəyyən bir sifariş statusuna (məs: CANCELED, DELIVERED) malik sifarişlərin miqdarını qaytarır
    @Override
    public OrderStatusResponse getOrderStatusStats(OrderStatus orderStatus) {
        log.info("Fetching order count for status category: {}", orderStatus);

        // Verilmiş status üzrə sifarişlərin sayını bazadan filtr edirik
        long count = orderRepo.countOrdersByStatus(orderStatus);

        return new OrderStatusResponse(orderStatus.name(), count);
    }

    // İstifadəçilər tərəfindən ən çox rəy (feedback) bildirilmiş məhsulların siyahısını gətirir
    @Override
    public List<TopProductResponse> getMostReviewedProducts() {
        log.info("Fetching statistics for products with the highest number of reviews");

        // Repository daxilindəki xüsusi sorğu vasitəsilə ən çox rəy alanları qaytarırıq
        return reviewRepo.getMostReviewedProducts();
    }
}