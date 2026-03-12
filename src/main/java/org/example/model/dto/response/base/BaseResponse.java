package org.example.model.dto.response.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.exception.BaseException;
import org.example.exception.types.NotFoundExceptionType;
import org.example.exception.types.NullNotAllowedExceptionType;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.example.model.dto.enums.response.ErrorResponseMessages.NOT_FOUND;
import static org.example.model.dto.enums.response.ErrorResponseMessages.NULL_NOT_ALLOWED;
import static org.example.model.dto.enums.response.SuccessResponseMessages.CREATED;
import static org.example.model.dto.enums.response.SuccessResponseMessages.SUCCESS;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
    HttpStatus httpStatus;
    Meta meta;
    T data;

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static final class Meta {
        String key;
        String message;

        public static Meta of(String key, String message) {
            return Meta.builder()
                    .key(key)
                    .message(message)
                    .build();
        }

        public static Meta responseOf(ResponseMessages responseMessages) {
            return of(responseMessages.key(), responseMessages.message());
        }

        public static Meta of(BaseException ex) {
            if (ex.getResponseMessages().equals(NOT_FOUND)) {
                NotFoundExceptionType notFoundData = ex.getNotFoundData();
                return of(
                        String.format(ex.getResponseMessages().key(), notFoundData.getTarget().toLowerCase()),
                        String.format(ex.getResponseMessages().message(), notFoundData.getTarget(), new ArrayList<>(notFoundData.getFields().values()))

                );
            }
            if (ex.getResponseMessages().equals(NULL_NOT_ALLOWED)) {
                NullNotAllowedExceptionType nullNotAllowed = ex.getNullNotAllowedData();

                return of(
                        String.format(ex.getResponseMessages().key(), nullNotAllowed.getTarget().toLowerCase()),
                        String.format(ex.getResponseMessages().message(), nullNotAllowed.getTarget())
                );

            }
            return responseOf(ex.getResponseMessages());
        }
    }

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .httpStatus(HttpStatus.OK)
                .meta(Meta.responseOf(SUCCESS))
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success() {
        return success(null);
    }

    public static <T> BaseResponse<T> created(T data) {
        return BaseResponse.<T>builder()
                .httpStatus(HttpStatus.CREATED)
                .data(data)
                .meta(Meta.responseOf(CREATED))
                .build();
    }

    public static BaseResponse<?> error(BaseException ex) {
        return BaseResponse.builder()
                .meta(Meta.of(ex))
                .httpStatus(ex.getResponseMessages().httpStatus())
                .build();
    }

    public static BaseResponse<?> error(SQLException ex) {
        return BaseResponse.builder()
                .meta(Meta.of(ex.getClass().getSimpleName(), ex.getMessage()))
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
    }

    public static BaseResponse<?> error(ExpiredJwtException ex) {
        return BaseResponse.builder()
                .meta(Meta.of(ex.getClass().getSimpleName(), ex.getMessage()))
                .httpStatus(HttpStatus.BAD_REQUEST)
                .build();
    }
}