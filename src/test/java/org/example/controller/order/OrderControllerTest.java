package org.example.controller.order;

import org.example.constant.TestConstants;
import org.example.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void checkout_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout"))
                .andExpect(status().isOk());
    }

    @Test
    void confirmPayment_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/{id}/confirm-payment", TestConstants.ORDER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void cancelPayment_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/{id}/cancel-payment", TestConstants.ORDER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void myOrders_ApiTest() throws Exception {
        mockMvc.perform(post("/api/v1/orders/my"))
                .andExpect(status().isOk());
    }
}