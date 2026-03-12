package org.example.model.dto.request.product;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.dto.enums.products.ConcernType;
import org.example.model.dto.enums.products.SkinType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFilterRequest {
    BigDecimal minPrice;
    BigDecimal maxPrice;
    Boolean isVegan;
    Boolean isForSensitiveSkin;
    SkinType skinType;
    ConcernType concernType;
}