package org.example.service.auth;

import org.example.model.dto.request.login.LoginRequestPayload;
import org.example.model.dto.request.login.UserRequestCreate;
import org.example.model.dto.response.login.LoginResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    LoginResponse registerUser(@RequestBody UserRequestCreate userRequestCreate);

    LoginResponse login(@RequestBody LoginRequestPayload payload);

    LoginResponse refreshToken(String refreshToken);

    void logout();

    void setAuthentication(String user);
}