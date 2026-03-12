package org.example.constant;

import java.math.BigDecimal;

public class TestConstants {

    // Pagination & Statistics
    public static final int PAGE = 0;
    public static final int SIZE = 10;
    public static final Long COUNT_VALUE = 100L; // OverviewResponse üçün
    public static final BigDecimal TOTAL_SUM = new BigDecimal("1500.50"); // Revenue üçün

    // Auth & User
    public static final Long USER_ID = 1L;
    public static final String USERNAME = "kylie_user";
    public static final String PASSWORD = "password123";
    public static final String FULL_NAME = "Kylie Jenner";
    public static final String ACCESS_TOKEN = "mock-access-token-123";
    public static final String REFRESH_TOKEN = "mock-refresh-token-456";

    // Product & Catalog
    public static final Long PRODUCT_ID = 1L;
    public static final String PRODUCT_NAME = "Matte Lipstick";
    public static final String SEARCH_WORD = "Lipstick";
    public static final BigDecimal PRODUCT_PRICE = new BigDecimal("100.00");
    public static final Integer STOCK_COUNT = 10;
    public static final Long BRAND_ID = 1L;

    // Filter Parameters
    public static final BigDecimal MIN_PRICE = new BigDecimal("50.00");
    public static final BigDecimal MAX_PRICE = new BigDecimal("500.00");

    // Order & Cart
    public static final Long ORDER_ID = 500L;
    public static final Integer QUANTITY = 2; // Həm Cart, həm AdminStats üçün
    public static final Long ITEM_ID = 1L;

    // Coupons, Reviews & Ratings
    public static final String COUPON_CODE = "SUMMER20";
    public static final BigDecimal DISCOUNT_VALUE = new BigDecimal("20.00");
    public static final Long REVIEW_ID = 1L;
    public static final Integer RATING = 5;
    public static final String REVIEW_COMMENT = "Məhsul əladır, çox bəyəndim!";
}