package org.example.demo13213.controller.cart;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/cart/"))
                .andExpect(status().isOk());
    }

    @Test
    void addCart_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items/")
                        .param("productId", TestConstants.PRODUCT_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void applyCoupon_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/cart/apply-coupon/")
                        .param("coupon", TestConstants.COUPON_CODE))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCart_ApiTest() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/delete-card/{id}", 1L))
                .andExpect(status().isOk());
    }
}