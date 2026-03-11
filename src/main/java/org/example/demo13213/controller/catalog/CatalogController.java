package org.example.demo13213.controller.catalog;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.model.dao.Products;
import org.example.demo13213.model.dto.response.base.BaseResponse;
import org.example.demo13213.model.dto.response.catalog.HomeCatalogResponse;
import org.example.demo13213.service.catalog.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Kataloq və məhsul siyahılarının idarə edilməsi üçün controller.
 Brendlərə görə filtrləmə və ana səhifə kataloq məlumatlarını təmin edir. */
@Slf4j
@RestController
@RequestMapping("/api/v1/catalog")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CatalogController {

    final CatalogService catalogService;

    // Müəyyən bir brendə aid olan bütün məhsulların siyahısını gətirir
    @GetMapping("/brands-with-products/{brandId}")
    public BaseResponse<List<Products>> findBrandWithProducts(@PathVariable Long brandId) {
        log.info("Request: Find products for brand ID: {}", brandId);
        return BaseResponse.success(catalogService.findBrandsWithProducts(brandId));
    }

    // Ana səhifə üçün kataloq məlumatlarını səhifələnmiş (pagination) şəkildə qaytarır
    @GetMapping("/home")
    public BaseResponse<HomeCatalogResponse> getHomeCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching home catalog data - Page: {}, Size: {}", page, size);
        return BaseResponse.success(catalogService.getHomeCatalog(page, size));
    }
}