package org.example.service.product;

import org.example.model.dao.Products;
import org.example.model.dto.request.product.ProductFilterRequest;
import org.example.model.dto.response.product.ProductResponseDetails;

import java.util.List;

public interface ProductService {
    List<Products> searchProduct(String productName);

    ProductResponseDetails getProductDetails(Long productId);

    List<Products> getBestSellers();

    List<Products> filter(ProductFilterRequest productFilterRequest);
}