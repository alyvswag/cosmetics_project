package org.example.model.dto.response.base;

import org.springframework.http.HttpStatus;

public interface ResponseMessages {
    String key();

    String message();

    HttpStatus httpStatus();
}