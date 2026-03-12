package org.example.model.dto.request.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequestCreate {
    Long productId;
    Integer rating;
    String comment;
}