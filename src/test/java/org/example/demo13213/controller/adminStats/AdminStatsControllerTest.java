package org.example.demo13213.controller.adminStats;

import org.example.demo13213.service.adminStats.AdminStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminStatsService adminStatsService;

    @Test
    void getOverview_ApiTest() throws Exception {
        mockMvc.perform(get("/api/admin/stats/overview")).andExpect(status().isOk());
    }

    @Test
    void getTopProducts_ApiTest() throws Exception {
        mockMvc.perform(get("/api/admin/stats/top-products")).andExpect(status().isOk());
    }

    @Test
    void getOrderStatusStats_ApiTest() throws Exception {
        mockMvc.perform(get("/api/admin/stats/order-status")
                        .param("orderStatus", "PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    void getMostReviewedProducts_ApiTest() throws Exception {
        mockMvc.perform(get("/api/admin/stats/most-reviewed")).andExpect(status().isOk());
    }
}