package org.example.controller.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.constant.TestConstants;
import org.example.model.dto.request.review.ReviewRequestCreate;
import org.example.model.dto.request.review.ReviewRequestUpdate;
import org.example.service.review.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReviewService reviewService;

    @Test
    void addReview_ApiTest() throws Exception {
        ReviewRequestCreate request = ReviewRequestCreate.builder()
                .productId(TestConstants.PRODUCT_ID)
                .rating(TestConstants.RATING)
                .comment(TestConstants.REVIEW_COMMENT)
                .build();

        mockMvc.perform(post("/api/v1/reviews/add-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getReviews_ApiTest() throws Exception {
        when(reviewService.getReviews(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/v1/reviews/get-reviews/{productId}", TestConstants.PRODUCT_ID))
                .andExpect(status().isOk());
    }

    @Test
    void updateReview_ApiTest() throws Exception {
        ReviewRequestUpdate request = ReviewRequestUpdate.builder()
                .reviewId(TestConstants.REVIEW_ID)
                .rating(5)
                .build();

        mockMvc.perform(post("/api/v1/reviews/update-review/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReview_ApiTest() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/delete-review/{productId}", TestConstants.PRODUCT_ID))
                .andExpect(status().isOk());
    }
}