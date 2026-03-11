package org.example.demo13213.service.catalog;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.model.dao.ProductInventory;
import org.example.demo13213.model.dao.Products;
import org.example.demo13213.model.dto.response.catalog.HomeCatalogResponse;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.product.ProductRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    ProductRepo productRepo;
    @Mock
    ProductInventoryRepo productInventoryRepo;

    @InjectMocks
    CatalogServiceImpl catalogService;

    @Test
    void findBrandsWithProducts_Success() {
        Products p = new Products();
        p.setId(TestConstants.PRODUCT_ID);

        List<Products> productsList = new ArrayList<>();
        productsList.add(p);

        ProductInventory inv = new ProductInventory();
        inv.setQuantity(10);

        when(productRepo.findByBrandId(TestConstants.BRAND_ID)).thenReturn(productsList);
        when(productInventoryRepo.findById(TestConstants.PRODUCT_ID)).thenReturn(Optional.of(inv));

        List<Products> result = catalogService.findBrandsWithProducts(TestConstants.BRAND_ID);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(productRepo, times(1)).findByBrandId(TestConstants.BRAND_ID);
    }

    @Test
    void getHomeCatalog_Success() {
        Products p1 = new Products();
        p1.setId(1L);
        Products p2 = new Products();
        p2.setId(2L);

        Page<Products> arrivalsPage = new PageImpl<>(new ArrayList<>(List.of(p1)));
        Page<Products> bestSellersPage = new PageImpl<>(new ArrayList<>(List.of(p2)));

        ProductInventory inv = new ProductInventory();
        inv.setQuantity(5);

        when(productRepo.findNewArrivals(any(Pageable.class))).thenReturn(arrivalsPage);
        when(productRepo.findBestSellers(any(Pageable.class))).thenReturn(bestSellersPage);
        when(productInventoryRepo.findById(anyLong())).thenReturn(Optional.of(inv));

        HomeCatalogResponse result = catalogService.getHomeCatalog(TestConstants.PAGE, TestConstants.SIZE);

        assertNotNull(result);
        assertEquals(1, result.getNewArrivals().size());
        assertEquals(1, result.getBestSellers().size());
        verify(productRepo, times(2)).findNewArrivals(any());
    }
}