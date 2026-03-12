package org.example.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.security.jwt.TokenProvider;
import org.example.service.auth.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.example.constant.TokenConstants.PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    final TokenProvider tokenProvider;
    final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.equals("/api/v1/products/search/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenRequest = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenRequest != null && tokenRequest.startsWith(PREFIX)) {
            String token = tokenRequest.substring(PREFIX.length());
            authService.setAuthentication(tokenProvider.getUsername(token));
        }

        filterChain.doFilter(request, response);
    }
}