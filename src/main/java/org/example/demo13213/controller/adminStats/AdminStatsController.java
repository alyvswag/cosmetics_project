package org.example.demo13213.controller.adminStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.model.dto.enums.order.OrderStatus;
import org.example.demo13213.model.dto.response.adminStats.OrderStatusResponse;
import org.example.demo13213.model.dto.response.adminStats.OverviewResponse;
import org.example.demo13213.model.dto.response.adminStats.TopProductResponse;
import org.example.demo13213.model.dto.response.base.BaseResponse;
import org.example.demo13213.service.adminStats.AdminStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* Admin paneli üçün statistik məlumatları təmin edən controller.
 Satışlar, top məhsullar və sifariş statusları haqqında məlumat gətirir. */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    // Ümumi statistikaya (gəlir, sifariş sayı və s.) baxış
    @GetMapping("/overview")
    public BaseResponse<OverviewResponse> getOverview() {
        log.info("Fetching admin dashboard overview stats");
        return BaseResponse.success(adminStatsService.getOverview());
    }

    // Ən çox satılan (populyar) məhsulların siyahısını gətirir
    @GetMapping("/top-products")
    public BaseResponse<List<TopProductResponse>> getTopProducts() {
        log.info("Request received for top selling products list");
        return BaseResponse.success(adminStatsService.getTopProducts());
    }

    // Müəyyən bir statusa (məsələn: PENDING, SHIPPED) görə sifarişlərin sayını gətirir
    @GetMapping("/order-status")
    public BaseResponse<OrderStatusResponse> getOrderStatusStats(@RequestParam OrderStatus orderStatus) {
        log.info("Fetching statistics for order status: {}", orderStatus);
        return BaseResponse.success(adminStatsService.getOrderStatusStats(orderStatus));
    }

    // Ən çox rəy (review) alan məhsulların statistikası
    @GetMapping("/most-reviewed")
    public BaseResponse<List<TopProductResponse>> getMostReviewedProducts() {
        log.info("Fetching most reviewed products statistics");
        return BaseResponse.success(adminStatsService.getMostReviewedProducts());
    }
}