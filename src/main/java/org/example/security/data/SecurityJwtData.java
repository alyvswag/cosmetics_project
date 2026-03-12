package org.example.security.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SecurityJwtData {
    String publicKey;
    String privateKey;
    Long accessTokenValidityTime;
    Long refreshTokenValidityTime;
}