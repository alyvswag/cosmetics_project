package org.example.demo13213.exception.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

/* Boş (null) olmaması gərəkən sahələr üçün xəta tipini müəyyən edir.
 Hansı hədəfin (target) null qaldığını bildirmək üçün istifadə olunur. */
@Slf4j
@Data
@Builder(access = PROTECTED)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@FieldDefaults(level = PRIVATE)
public class NullNotAllowedExceptionType {
    String target;

    // Null xətası obyektini yaratmaq üçün köməkçi static metod
    public static NullNotAllowedExceptionType of(String target) {
        log.info("Null check failed for target: [{}]", target);
        return NullNotAllowedExceptionType.builder()
                .target(target)
                .build();
    }
}