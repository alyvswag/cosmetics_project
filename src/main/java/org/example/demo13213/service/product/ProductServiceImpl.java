package org.example.demo13213.service.product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.demo13213.exception.BaseException;
import org.example.demo13213.model.dao.*;
import org.example.demo13213.model.dto.request.product.ProductFilterRequest;
import org.example.demo13213.model.dto.response.product.ProductResponseDetails;
import org.example.demo13213.repo.coupon.UserCouponRepo;
import org.example.demo13213.repo.order.OrderItemRepo;
import org.example.demo13213.repo.product.ProductInventoryRepo;
import org.example.demo13213.repo.product.ProductRepo;
import org.example.demo13213.repo.review.ReviewRepo;
import org.example.demo13213.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.example.demo13213.model.dto.enums.response.ErrorResponseMessages.PRODUCT_OUT_OF_STOCK;

/* Məhsulların idarə edilməsi, axtarışı, detallı məlumatların gətirilməsi
 və dinamik filterləmə əməliyyatlarını icra edən service. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    final ProductRepo productRepo;
    final ProductInventoryRepo productInventoryRepo;
    final ReviewRepo reviewRepo;
    final UserCouponRepo userCouponRepo;
    final OrderItemRepo orderItemRepo;
    final EntityManager em;

    // Məhsul adına görə axtarış edir və stok vəziyyətini yoxlayır
    @Override
    public List<Products> searchProduct(String productName) {
        log.info("Initiating product search for term: [{}]", productName);

        List<Products> products = productRepo.searchProductsByName(productName);
        checkInventory(products);

        log.info("Search completed. Found {} matching and in-stock products", products.size());
        return products;
    }

    // Məhsulun reytinq, rəy və fərdi kuponlar tətbiq edilmiş qiymət detallarını hazırlayır
    @Override
    public ProductResponseDetails getProductDetails(Long productId) {
        log.info("Processing product details request for ID: {}", productId);

        // 1. Məhsulun mövcudluğunun yoxlanılması
        Products product = productRepo.findByIdForProduct(productId).orElseThrow(() -> {
            log.error("Detail retrieval failed: Product ID {} not found", productId);
            return BaseException.notFound("product", productId.toString(), productId);
        });

        log.debug("Found product: {}", product.getName());

        // 2. Stok məlumatlarının əldə edilməsi
        ProductInventory productQuantity = productInventoryRepo.findByIdForProductQuantity(productId).orElseThrow(() -> {
            log.warn("Inventory check: Product ID {} exists but has no stock record", productId);
            return BaseException.of(PRODUCT_OUT_OF_STOCK);
        });

        log.debug("Current inventory level for product {}: {}", productId, productQuantity.getQuantity());

        // 3. Rəylərin gətirilməsi və orta reytinqin hesablanması
        List<Reviews> reviews = reviewRepo.findByProductIdForReview(productId);
        log.debug("Found {} reviews for product {}", reviews.size(), productId);

        double avgRating = 0;
        if (!reviews.isEmpty()) {
            avgRating = reviews.stream()
                    .mapToInt(Reviews::getRating)
                    .average()
                    .orElse(0);
        }

        log.debug("Calculated average rating: {}", avgRating);

        // 4. Sessiyadakı istifadəçinin identifikasiyası
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        log.trace("Calculating discounts for user: {}", userPrincipal.getUsername());

        // 5. İstifadəçiyə aid aktiv kuponların analizi
        List<UserCoupons> coupons = userCouponRepo.findActiveByUserIdForUserCoupon(userPrincipal.getId());
        log.debug("Retrieved {} active coupons for user {}", coupons.size(), userPrincipal.getId());

        // 6. Qiymət hesablama məntiqi (Kupon tətbiqi)
        BigDecimal finalPrice = product.getPrice();
        OffsetDateTime now = OffsetDateTime.now();

        for (UserCoupons userCoupon : coupons) {
            Coupons coupon = userCoupon.getCoupon();

            // Kateqoriya və vaxt etibarlılığının yoxlanılması
            if (coupon.getCategory() != null && coupon.getCategory().getId().equals(product.getCategory().getId())) {
                OffsetDateTime activatedAt = userCoupon.getActivatedAt();
                int activeDays = coupon.getActiveDays() != null ? coupon.getActiveDays() : 0;
                OffsetDateTime expiryDate = activatedAt.plusDays(activeDays);

                if (now.isBefore(expiryDate)) {
                    finalPrice = finalPrice.subtract(coupon.getDiscountValue());
                }
            }
        }

        // Qiymətin mənfi olmamasının təmini
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        // 7. Response DTO-nun formalaşdırılması
        ProductResponseDetails response = new ProductResponseDetails();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setFinalPrice(finalPrice);
        response.setInStock(productQuantity.getQuantity() > 0);
        response.setAvgRating(avgRating);
        response.setCategory(product.getCategory().getName());

        log.info("Product details for ID {} successfully generated", productId);
        return response;
    }

    // Ən çox satılan məhsulları müəyyən edir və stok yoxlamasından keçirir
    @Override
    public List<Products> getBestSellers() {
        log.info("Fetching global best sellers list");
        List<Object[]> bestSellersRaw = orderItemRepo.findBestSellingProducts();

        List<Long> productIds = bestSellersRaw.stream()
                .map(obj -> (Long) obj[0])
                .toList();

        if (productIds.isEmpty()) {
            log.warn("Best sellers list is empty");
            return List.of();
        }

        List<Products> products = productRepo.findAllById(productIds);

        // Satış statistikasına uyğun sıralamanın bərpası
        products.sort(Comparator.comparingInt((Products p) -> {
            Long pid = p.getId();
            Object[] match = bestSellersRaw.stream()
                    .filter(obj -> obj[0].equals(pid))
                    .findFirst()
                    .orElse(null);
            return match == null ? 0 : ((Number) match[1]).intValue();
        }).reversed());

        checkInventory(products);
        log.info("Returned {} best selling products", products.size());
        return products;
    }

    // Criteria API vasitəsilə mürəkkəb filterləmə sorğularını icra edir
    @Override
    public List<Products> filter(ProductFilterRequest productFilterRequest) {
        log.info("Applying dynamic filters: {}", productFilterRequest);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Products> query = cb.createQuery(Products.class);
        Root<Products> root = query.from(Products.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("isActive")));

        // Qiymət filterləri
        if (productFilterRequest.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), productFilterRequest.getMinPrice()));
        }
        if (productFilterRequest.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), productFilterRequest.getMaxPrice()));
        }

        // Məhsul xüsusiyyətləri üzrə filterlər
        if (productFilterRequest.getIsVegan() != null) {
            predicates.add(cb.equal(root.get("isVegan"), productFilterRequest.getIsVegan()));
        }
        if (productFilterRequest.getIsForSensitiveSkin() != null) {
            predicates.add(cb.equal(root.get("isForSensitiveSkin"), productFilterRequest.getIsForSensitiveSkin()));
        }
        if (productFilterRequest.getSkinType() != null) {
            predicates.add(cb.equal(root.get("skinType"), productFilterRequest.getSkinType()));
        }
        if (productFilterRequest.getConcernType() != null) {
            predicates.add(cb.equal(root.get("concernType"), productFilterRequest.getConcernType()));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<Products> typedQuery = em.createQuery(query);
        List<Products> resultList = typedQuery.getResultList();

        checkInventory(resultList);
        log.info("Filtering complete. Matches found: {}", resultList.size());

        return resultList;
    }

    // Siyahıdakı məhsulların stok mövcudluğunu yoxlayır və tapılmayanları kənarlaşdırır
    private void checkInventory(List<Products> products) {
        products.removeIf(product -> {
            ProductInventory inventory = productInventoryRepo.findById(product.getId())
                    .orElseThrow(() -> {
                        log.error("Critical Inconsistency: No inventory record for product ID {}", product.getId());
                        return BaseException.notFound(ProductInventory.class.getSimpleName(),
                                "productId", String.valueOf(product.getId()));
                    });
            return inventory.getQuantity() <= 0;
        });
    }
}