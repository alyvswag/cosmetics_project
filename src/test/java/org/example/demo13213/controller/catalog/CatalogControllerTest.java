package org.example.demo13213.controller.catalog;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.service.catalog.CatalogService;
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
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void findBrandWithProducts_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/brands-with-products/{brandId}", TestConstants.BRAND_ID))
                .andExpect(status().isOk());
    }

    @Test
    void getHomeCatalog_ApiTest() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/home")
                        .param("page", String.valueOf(TestConstants.PAGE))
                        .param("size", String.valueOf(TestConstants.SIZE)))
                .andExpect(status().isOk());
    }
}