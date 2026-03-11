package org.example.demo13213.repo.review;

import org.example.demo13213.model.dao.Reviews;
import org.example.demo13213.model.dto.response.adminStats.TopProductResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<Reviews, Long> {
    @Query("SELECT r FROM Reviews r WHERE r.product.id = :productId ")
    List<Reviews> findByProductIdForReview(@Param("productId") Long productId);

    @Query("Select r from Reviews  r where r.id=:reviewId")
    Optional<Reviews> findByReviewId(Long reviewId);

    @Query("SELECT new org.example.demo13213.model.dto.response.adminStats.TopProductResponse(r.product.name, CAST(COUNT(r) AS int)) FROM Reviews r GROUP BY r.product.name ORDER BY COUNT(r) DESC")
    List<TopProductResponse> getMostReviewedProducts();
}