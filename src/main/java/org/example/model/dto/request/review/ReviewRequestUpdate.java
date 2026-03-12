package org.example.model.dto.request.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequestUpdate {
    Long reviewId;
    Integer rating;
    String comment;
}