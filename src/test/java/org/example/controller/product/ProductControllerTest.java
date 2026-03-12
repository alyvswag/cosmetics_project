package org.example.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.constant.TestConstants;
import org.example.model.dto.request.product.ProductFilterRequest;
import org.example.service.product.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void search_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/products/search/{searchWord}", TestConstants.SEARCH_WORD))
                .andExpect(status().isOk());
    }

    @Test
    void getProductDetails_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}/details", TestConstants.PRODUCT_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getBestSellers_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/products/bestsellers"))
                .andExpect(status().isOk());
    }

    @Test
    void filterProducts_ApiTest() throws Exception {
        ProductFilterRequest request = ProductFilterRequest.builder()
                .minPrice(TestConstants.MIN_PRICE)
                .maxPrice(TestConstants.MAX_PRICE)
                .build();

        mockMvc.perform(post("/api/v1/products/filter-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}