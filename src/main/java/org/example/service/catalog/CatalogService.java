package org.example.service.catalog;

import org.example.model.dao.Products;
import org.example.model.dto.response.catalog.HomeCatalogResponse;

import java.util.List;

public interface CatalogService {
    List<Products> findBrandsWithProducts(Long brandId);

    HomeCatalogResponse getHomeCatalog(int page, int size);
}