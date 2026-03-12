package org.example.service.catalog;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.BaseException;
import org.example.model.dao.ProductInventory;
import org.example.model.dao.Products;
import org.example.model.dto.response.catalog.HomeCatalogResponse;
import org.example.repo.product.ProductInventoryRepo;
import org.example.repo.product.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/* Kataloq məlumatlarının (yeni gələnlər, çox satılanlar, brendə görə axtarış)
 idarə edilməsi və stok yoxlanışı proseslərini həyata keçirən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class CatalogServiceImpl implements CatalogService {

    final ProductRepo productRepo;
    final ProductInventoryRepo productInventoryRepo;

    // Müəyyən bir brendə aid olan aktiv və stokda olan məhsulları gətirir
    @Override
    public List<Products> findBrandsWithProducts(Long brandId) {
        log.info("Processing request to fetch products for brand ID: {}", brandId);

        // 1. Brendə aid məhsulların bazadan çəkilməsi
        List<Products> products = productRepo.findByBrandId(brandId);

        if (products.isEmpty()) {
            log.warn("Search result: No products found for brand ID: {}", brandId);
            throw BaseException.notFound("brand", "id", brandId.toString());
        }

        log.debug("Found {} potential products for brand {}. Proceeding to inventory check...", products.size(), brandId);

        // 2. Məhsulların satışa yararlı olub-olmadığının (stok) yoxlanılması
        checkInventory(products);

        log.info("Successfully retrieved {} available products for brand ID: {}", products.size(), brandId);
        return products;
    }

    // Ana səhifə üçün "Yeni gələnlər" və "Bestseller" siyahılarını hazırlayır
    @Override
    public HomeCatalogResponse getHomeCatalog(int page, int size) {
        log.info("Initiating home catalog data load [Page: {}, Size: {}]", page, size);

        // 1. Yeni gələn məhsulların (New Arrivals) əldə edilməsi
        // Yaranma tarixinə görə azalan sıra ilə (descending) səhifələmə tətbiq olunur
        log.debug("Fetching new arrivals with descending sort by creation date");
        Pageable newArrivalsPageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Products> newArrivals = productRepo.findNewArrivals(newArrivalsPageable);
        checkInventory(newArrivals.getContent());

        // 2. Ən çox satılan məhsulların (Bestsellers) əldə edilməsi
        // Sıralama məntiqi birbaşa repository daxilindəki custom query ilə idarə olunur
        log.debug("Fetching best selling products page data");
        Pageable bestSellersPageable = PageRequest.of(page, size);
        Page<Products> bestSellersPage = productRepo.findBestSellers(bestSellersPageable);
        checkInventory(bestSellersPage.getContent());

        log.info("Home catalog composition complete. New Arrivals count: {}, Best Sellers count: {}",
                newArrivals.getContent().size(), bestSellersPage.getContent().size());

        // 3. Toplanmış məlumatların vahid response obyektinə inşası
        return HomeCatalogResponse.builder()
                .newArrivals(newArrivals.getContent())
                .bestSellers(bestSellersPage.getContent())
                .build();
    }

    // Məhsul siyahısından stokda (inventory) olmayanları avtomatik təmizləyir
    private void checkInventory(List<Products> products) {
        log.trace("Executing inventory validation for {} items", products.size());

        products.removeIf(product -> {
            // Məhsulun anbar qeydinin tapılması
            ProductInventory inventory = productInventoryRepo.findById(product.getId())
                    .orElseThrow(() -> {
                        log.error("Data Integrity Error: Inventory record missing for product ID: {}", product.getId());
                        return BaseException.notFound(ProductInventory.class.getSimpleName(),
                                "productId", String.valueOf(product.getId()));
                    });

            // Miqdarın sıfır və ya mənfi olub-olmadığının yoxlanılması
            boolean outOfStock = inventory.getQuantity() <= 0;
            if (outOfStock) {
                log.debug("Excluding Product ID: {} from results - Current stock: {}", product.getId(), inventory.getQuantity());
            }
            return outOfStock;
        });
    }
}