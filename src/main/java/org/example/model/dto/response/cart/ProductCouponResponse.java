package org.example.model.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.dao.Products;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCouponResponse {
    Products p;
    BigDecimal discValue;
    BigDecimal finalPrice;
}