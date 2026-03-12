package org.example.model.dto.response.adminStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class OrderStatusResponse {
    private String status;
    private Long count;
}