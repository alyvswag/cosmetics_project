package org.example.demo13213.service.review;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.exception.BaseException;
import org.example.demo13213.model.dao.Products;
import org.example.demo13213.model.dao.Reviews;
import org.example.demo13213.model.dao.Users;
import org.example.demo13213.model.dto.request.review.ReviewRequestCreate;
import org.example.demo13213.model.dto.request.review.ReviewRequestUpdate;
import org.example.demo13213.repo.product.ProductRepo;
import org.example.demo13213.repo.review.ReviewRepo;
import org.example.demo13213.repo.user.UserRepo;
import org.example.demo13213.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/* Məhsullara yazılan rəylərin yaradılması, oxunması,
 yenilənməsi və silinməsi əməliyyatlarını idarə edən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    final UserRepo userRepo;
    final ProductRepo productRepo;
    final ReviewRepo reviewRepo;

    // Yeni məhsul rəyi əlavə edir
    @Override
    public Reviews addReview(ReviewRequestCreate requestCreate) {
        log.info("Initiating process to add a new review for product ID: {}", requestCreate.getProductId());

        // 1. Sessiyadakı istifadəçinin əldə edilməsi
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Users u = userRepo.findUserByUsername(user.getUsername())
                .orElseThrow(() -> {
                    log.error("Authentication inconsistency: User [{}] not found in database", user.getUsername());
                    return BaseException.notFound(Users.class.getSimpleName(), "username", user.getUsername());
                });

        // 2. Rəy yazılacaq məhsulun mövcudluğunun yoxlanılması
        Products product = productRepo.findByIdForProduct(requestCreate.getProductId()).orElseThrow(() -> {
            log.error("Review creation failed: Product ID {} not found", requestCreate.getProductId());
            return BaseException.notFound("product", requestCreate.getProductId().toString(), requestCreate.getProductId());
        });

        // 3. Rəy obyektinin formalaşdırılması və yadda saxlanılması
        Reviews r = new Reviews();
        r.setUser(u);
        r.setProduct(product);
        r.setRating(requestCreate.getRating());
        r.setComment(requestCreate.getComment());

        Reviews savedReview = reviewRepo.save(r);
        log.info("Review successfully created with ID: {} by user: {}", savedReview.getId(), user.getUsername());

        return savedReview;
    }

    // Müvafiq məhsula aid olan bütün rəylərin siyahısını gətirir
    @Override
    public List<Reviews> getReviews(Long productId) {
        log.info("Fetching all reviews for product ID: {}", productId);
        List<Reviews> reviews = reviewRepo.findByProductIdForReview(productId);
        log.debug("Found {} reviews for product ID: {}", reviews.size(), productId);
        return reviews;
    }

    // Mövcud rəyin məzmununu və reytinqini yeniləyir
    @Override
    public void updateReview(ReviewRequestUpdate requestUpdate) {
        log.info("Updating review ID: {}", requestUpdate.getReviewId());

        Reviews r = reviewRepo.findByReviewId(requestUpdate.getReviewId())
                .orElseThrow(() -> {
                    log.error("Update failed: Review ID {} not found", requestUpdate.getReviewId());
                    return BaseException.notFound("review", requestUpdate.getReviewId().toString(), requestUpdate.getReviewId());
                });

        r.setRating(requestUpdate.getRating());
        r.setComment(requestUpdate.getComment());

        reviewRepo.save(r);
        log.info("Review ID: {} successfully updated", requestUpdate.getReviewId());
    }

    // Rəyi identifikatoruna əsasən sistemdən silir
    @Override
    public void deleteReview(Long id) {
        log.info("Attempting to delete review ID: {}", id);
        reviewRepo.deleteById(id);
        log.info("Review ID: {} deleted from the system", id);
    }
}