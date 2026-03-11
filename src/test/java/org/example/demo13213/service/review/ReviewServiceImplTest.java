package org.example.demo13213.service.review;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.model.dao.Products;
import org.example.demo13213.model.dao.Reviews;
import org.example.demo13213.model.dao.Users;
import org.example.demo13213.model.dto.request.review.ReviewRequestCreate;
import org.example.demo13213.model.dto.request.review.ReviewRequestUpdate;
import org.example.demo13213.repo.product.ProductRepo;
import org.example.demo13213.repo.review.ReviewRepo;
import org.example.demo13213.repo.user.UserRepo;
import org.example.demo13213.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    UserRepo userRepo;
    @Mock
    ProductRepo productRepo;
    @Mock
    ReviewRepo reviewRepo;

    @InjectMocks
    ReviewServiceImpl reviewService;

    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        UserPrincipal principal = new UserPrincipal(TestConstants.USER_ID, TestConstants.USERNAME, TestConstants.PASSWORD);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(principal);
    }

    @Test
    void addReview_Success() {
        ReviewRequestCreate request = ReviewRequestCreate.builder()
                .productId(TestConstants.PRODUCT_ID)
                .rating(TestConstants.RATING)
                .comment(TestConstants.REVIEW_COMMENT)
                .build();

        Users mockUser = new Users();
        mockUser.setUsername(TestConstants.USERNAME);

        Products mockProduct = new Products();
        mockProduct.setId(TestConstants.PRODUCT_ID);

        when(userRepo.findUserByUsername(any())).thenReturn(Optional.of(mockUser));
        when(productRepo.findByIdForProduct(any())).thenReturn(Optional.of(mockProduct));
        when(reviewRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Reviews result = reviewService.addReview(request);

        assertNotNull(result);
        assertEquals(TestConstants.REVIEW_COMMENT, result.getComment());
        verify(reviewRepo, times(1)).save(any());
    }

    @Test
    void getReviews_Success() {
        when(reviewRepo.findByProductIdForReview(any())).thenReturn(List.of(new Reviews()));
        List<Reviews> result = reviewService.getReviews(TestConstants.PRODUCT_ID);
        assertFalse(result.isEmpty());
    }

    @Test
    void updateReview_Success() {
        ReviewRequestUpdate request = ReviewRequestUpdate.builder()
                .reviewId(TestConstants.REVIEW_ID)
                .rating(4)
                .comment("Updated")
                .build();

        Reviews existingReview = new Reviews();
        existingReview.setId(TestConstants.REVIEW_ID);

        when(reviewRepo.findByReviewId(any())).thenReturn(Optional.of(existingReview));

        reviewService.updateReview(request);

        assertEquals(4, existingReview.getRating());
        verify(reviewRepo, times(1)).save(any());
    }

    @Test
    void deleteReview_Success() {
        doNothing().when(reviewRepo).deleteById(any());
        reviewService.deleteReview(TestConstants.REVIEW_ID);
        verify(reviewRepo, times(1)).deleteById(TestConstants.REVIEW_ID);
    }
}