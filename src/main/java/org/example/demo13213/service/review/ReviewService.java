package org.example.demo13213.service.review;

import org.example.demo13213.model.dao.Reviews;
import org.example.demo13213.model.dto.request.review.ReviewRequestCreate;
import org.example.demo13213.model.dto.request.review.ReviewRequestUpdate;

import java.util.List;

public interface ReviewService {
    Reviews addReview(ReviewRequestCreate requestCreate);

    List<Reviews> getReviews(Long productId);

    void updateReview(ReviewRequestUpdate requestUpdate);

    void deleteReview(Long id);
}