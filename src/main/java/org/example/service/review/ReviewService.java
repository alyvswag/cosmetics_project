package org.example.service.review;

import org.example.model.dao.Reviews;
import org.example.model.dto.request.review.ReviewRequestCreate;
import org.example.model.dto.request.review.ReviewRequestUpdate;

import java.util.List;

public interface ReviewService {
    Reviews addReview(ReviewRequestCreate requestCreate);

    List<Reviews> getReviews(Long productId);

    void updateReview(ReviewRequestUpdate requestUpdate);

    void deleteReview(Long id);
}