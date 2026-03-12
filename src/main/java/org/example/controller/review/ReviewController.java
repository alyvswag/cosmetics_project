package org.example.controller.review;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dao.Reviews;
import org.example.model.dto.request.review.ReviewRequestCreate;
import org.example.model.dto.request.review.ReviewRequestUpdate;
import org.example.model.dto.response.base.BaseResponse;
import org.example.service.review.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Məhsullara yazılan rəylərin və xalların (rating) idarə edilməsi üçün controller.
 Rəy əlavə etmə, oxuma, yeniləmə və silmə əməliyyatlarını təmin edir. */
@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewController {

    final ReviewService reviewService;

    // Məhsul üçün yeni rəy və reytinq xalı əlavə edir
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add-review")
    public BaseResponse<Reviews> addReview(@RequestBody ReviewRequestCreate ratingAndReview) {
        log.info("Adding new review for product ID: {}", ratingAndReview.getProductId());
        return BaseResponse.created(reviewService.addReview(ratingAndReview));
    }

    // Müəyyən bir məhsula aid olan bütün rəylərin siyahısını gətirir
    @GetMapping("/get-reviews/{productId}")
    public BaseResponse<List<Reviews>> getReviews(@PathVariable Long productId) {
        log.info("Fetching all reviews for product ID: {}", productId);
        return BaseResponse.success(reviewService.getReviews(productId));
    }

    // Mövcud rəyin məzmununu və ya xalını yeniləyir
    @PostMapping("/update-review/")
    public BaseResponse<Void> updateReview(@RequestBody ReviewRequestUpdate requestUpdate) {
        log.info("Updating existing review");
        reviewService.updateReview(requestUpdate);
        return BaseResponse.success();
    }

    // İstifadəçinin konkret bir məhsula yazdığı rəyi sistemdən silir
    @DeleteMapping("/delete-review/{productId}")
    public BaseResponse<Void> deleteReview(@PathVariable Long productId) {
        log.info("Request to delete review for product ID: {}", productId);
        reviewService.deleteReview(productId);
        return BaseResponse.success();
    }
}