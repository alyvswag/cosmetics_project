package org.example.security.jwt;

import java.util.List;

public interface TokenService<T, V> {
    List<String> generate(T obj);

    V read(String token);

    String getUsername(String token);
}