package org.example.exception;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.response.base.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/* Proqram daxilində yaranan bütün xətaların (Exception) bir mərkəzdən tutulub,
 müştəriyə (client) uyğun formatda cavab qaytarılmasını təmin edir. */
@Slf4j
@RestControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {

    // Layihə üçün yaratdığımız xüsusi BaseException xətalarını idarə edir
    @ExceptionHandler
    public ResponseEntity<BaseResponse<?>> handleBaseException(BaseException ex) {
        log.error("BaseException caught: status={}, message={}", ex.getResponseMessages().httpStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getResponseMessages().httpStatus()).body(BaseResponse.error(ex));
    }

    // Verilənlər bazası (SQL) ilə bağlı yaranan xətaları idarə edir
    @ExceptionHandler
    public ResponseEntity<BaseResponse<?>> handleBaseException(SQLException ex) {
        log.error("Database error occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.error(ex));
    }

    // Vaxtı keçmiş və ya etibarsız JWT token xətalarını idarə edir
    @ExceptionHandler
    public ResponseEntity<BaseResponse<?>> handleBaseException(ExpiredJwtException ex) {
        log.warn("JWT Token expired: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.error(ex));
    }
}