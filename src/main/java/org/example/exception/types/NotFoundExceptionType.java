package org.example.exception.types;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static lombok.AccessLevel.*;

/* Tapılmayan resurslar (məsələn: məhsul, istifadəçi) üçün xəta detallarını saxlayan tip.
 Hansı hədəfin (target) və hansı sahələrin (fields) tapılmadığını strukturlaşdırılmış şəkildə saxlayır. */
@Slf4j
@Data
@Builder(access = PROTECTED)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE)
public class NotFoundExceptionType {
    String target;
    Map<String, Object> fields;

    // Yeni bir NotFoundExceptionType obyekti yaratmaq üçün köməkçi static metod
    public static NotFoundExceptionType of(String target, Map<String, Object> fields) {
        log.info("Creating NotFoundExceptionType for target: [{}]", target);
        return NotFoundExceptionType.builder()
                .fields(fields)
                .target(target)
                .build();
    }
}