package org.example.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.types.NotFoundExceptionType;
import org.example.exception.types.NullNotAllowedExceptionType;
import org.example.model.dto.response.base.ResponseMessages;

import java.util.Map;

import static org.example.model.dto.enums.response.ErrorResponseMessages.NOT_FOUND;

/* Layihədəki bütün xüsusi (custom) xətaların əsasını təşkil edən baza xəta class-ı.
 Xəta mesajlarını, tapılmayan dataları və null validasiya xətalarını mərkəzləşdirilmiş şəkildə idarə edir. */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {
    ResponseMessages responseMessages;
    NotFoundExceptionType notFoundData;
    NullNotAllowedExceptionType nullNotAllowedData;

    // Xətanın əsas mesajını qaytarır
    @Override
    public String getMessage() {
        return responseMessages.message();
    }

    // Ümumi xəta mesajları üçün BaseException obyekti yaradır
    public static BaseException of(ResponseMessages responseMessage) {
        log.error("BaseException thrown with message: {}", responseMessage.message());
        return BaseException.builder()
                .responseMessages(responseMessage)
                .build();
    }

    // Tapılmayan resurslar (404) üçün strukturlaşdırılmış xəta yaradır
    public static BaseException notFound(String target, String field, Object value) {
        log.warn("Resource not found - Target: [{}], Field: [{}], Value: [{}]", target, field, value);
        return BaseException.builder()
                .responseMessages(NOT_FOUND)
                .notFoundData(
                        NotFoundExceptionType.of(target, Map.of(field, value))
                )
                .build();
    }
}