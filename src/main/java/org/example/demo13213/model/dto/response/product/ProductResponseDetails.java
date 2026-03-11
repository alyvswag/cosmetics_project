package org.example.demo13213.model.dto.response.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseDetails {
    Long id;
    String name;
    String description;
    BigDecimal price;          // ilkin qiymət
    BigDecimal finalPrice;     // endirimdən sonra qiymət
    Boolean inStock;           // stokda var ya yox
    Double avgRating;          // ortalama reytinq
    String category;           // review-ların siyahısı
}