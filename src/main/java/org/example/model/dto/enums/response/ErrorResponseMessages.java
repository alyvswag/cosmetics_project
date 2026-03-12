package org.example.model.dto.enums.response;

import org.example.model.dto.response.base.ResponseMessages;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum ErrorResponseMessages implements ResponseMessages {
    UNEXPECTED("unexpected", "Unexpected error.", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND("not_found_%s", "The requested %s model with %s was not found.", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK("product_out_of_stock", "The requested product is out of stock or not available in sufficient quantity.", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_REGISTERED("username_already_registered", "Username already registered", HttpStatus.CONFLICT),
    NULL_NOT_ALLOWED("null_not_allowed_%s", "The column %s does not allow null values.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("invalid_token", "The provided token is no longer valid.", HttpStatus.UNAUTHORIZED),
    WRONG_TOKEN("wrong_token", "The provided token does not belong to us.", HttpStatus.UNAUTHORIZED),
    INVALID_USERNAME_OR_PASSWORD("invalid_username_or_password", "The provided username or password is incorrect.", HttpStatus.UNAUTHORIZED),
    COUPON_NOT_APPLICABLE("coupon_not_applicable", "This coupon does not apply to any products in your cart.", HttpStatus.BAD_REQUEST),
    CART_EMPTY("cart_empty", "Your cart is empty.", HttpStatus.BAD_REQUEST),
    ORDER_EMPTY("order_empty", "Your order is empty.", HttpStatus.BAD_REQUEST),
    ;

    String key;
    String message;
    HttpStatus httpStatus;

    @Override
    public String key() {
        return key;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}