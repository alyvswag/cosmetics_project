package org.example.controller.product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dao.Products;
import org.example.model.dto.request.product.ProductFilterRequest;
import org.example.model.dto.response.base.BaseResponse;
import org.example.model.dto.response.product.ProductResponseDetails;
import org.example.service.product.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Məhsulların axtarışı, detallarının gətirilməsi, ən çox satılanlar
 və müxtəlif filtrlər üzrə çeşidlənməsi əməliyyatlarını idarə edən controller. */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductController {

    final ProductService productService;

    // Daxil edilən açar sözə əsasən məhsulların axtarışını həyata keçirir
    @GetMapping("/search/{searchWord}")
    public BaseResponse<List<Products>> search(@PathVariable String searchWord) {
        log.info("Searching for products with keyword: {}", searchWord);
        return BaseResponse.success(productService.searchProduct(searchWord));
    }

    // Konkret bir məhsulun ID-sinə görə bütün detallarını qaytarır
    @GetMapping("/{id}/details")
    public BaseResponse<ProductResponseDetails> getProductDetails(@PathVariable Long id) {
        log.info("Fetching detailed information for product ID: {}", id);
        return BaseResponse.success(productService.getProductDetails(id));
    }

    // Sistemdə ən çox satılan (bestseller) məhsulların siyahısını gətirir
    @GetMapping("/bestsellers")
    public BaseResponse<List<Products>> getBestSellers() {
        log.info("Request received for best-selling products");
        return BaseResponse.success(productService.getBestSellers());
    }

    // Qiymət, marka və digər meyarlara görə məhsulları filtrləyir
    @PostMapping("/filter-products")
    public BaseResponse<List<Products>> filterProducts(@RequestBody ProductFilterRequest productFilterRequest) {
        log.info("Filtering products based on provided criteria");
        return BaseResponse.success(productService.filter(productFilterRequest));
    }
}