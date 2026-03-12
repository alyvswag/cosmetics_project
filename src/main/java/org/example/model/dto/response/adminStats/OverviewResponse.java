package org.example.model.dto.response.adminStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverviewResponse {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long totalCustomers;
}