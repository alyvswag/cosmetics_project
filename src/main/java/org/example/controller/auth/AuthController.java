package org.example.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.request.login.LoginRequestPayload;
import org.example.model.dto.request.login.UserRequestCreate;
import org.example.model.dto.response.base.BaseResponse;
import org.example.model.dto.response.login.LoginResponse;
import org.example.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/* İstifadəçilərin giriş, qeydiyyat və sessiya idarəetməsi (token yeniləmə, logout)
 əməliyyatlarını həyata keçirən controller. */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // İstifadəçinin sistemə daxil olması və JWT token alması üçün endpoint
    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequestPayload loginRequestPayload) {
        log.info("Login request received for user: {}", loginRequestPayload.getUsername());
        return BaseResponse.success(authService.login(loginRequestPayload));
    }

    // Yeni istifadəçi qeydiyyatı
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register-user")
    public BaseResponse<LoginResponse> registerUser(@RequestBody UserRequestCreate userRequestCreate) {
        log.info("Registering new user with username: {}", userRequestCreate.getUsername());
        return BaseResponse.created(authService.registerUser(userRequestCreate));
    }

    // Mövcud refresh token vasitəsilə yeni access token əldə edilməsi
    @PostMapping("/refresh-token/{refreshToken}")
    public BaseResponse<LoginResponse> refreshToken(@PathVariable("refreshToken") String refreshToken) {
        log.info("Token refresh process started");
        return BaseResponse.success(authService.refreshToken(refreshToken));
    }

    // İstifadəçinin sistemdən çıxış etməsi və sessiyanın sonlandırılması
    @PostMapping("/logout")
    public BaseResponse<Void> logout() {
        log.info("Logout request processed");
        authService.logout();
        return BaseResponse.success();
    }
}