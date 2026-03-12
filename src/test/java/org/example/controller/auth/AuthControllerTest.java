package org.example.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.constant.TestConstants;
import org.example.model.dto.request.login.LoginRequestPayload;
import org.example.model.dto.request.login.UserRequestCreate;
import org.example.model.dto.response.login.LoginResponse;
import org.example.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_Success() throws Exception {
        LoginRequestPayload payload = new LoginRequestPayload();
        payload.setUsername(TestConstants.USERNAME);
        payload.setPassword(TestConstants.PASSWORD);

        LoginResponse response = LoginResponse.builder()
                .accessToken(TestConstants.ACCESS_TOKEN)
                .refreshToken(TestConstants.REFRESH_TOKEN)
                .build();

        when(authService.login(any(LoginRequestPayload.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(TestConstants.ACCESS_TOKEN));
    }

    @Test
    void registerUser_Success() throws Exception {
        UserRequestCreate request = new UserRequestCreate();
        request.setUsername(TestConstants.USERNAME);
        request.setPassword(TestConstants.PASSWORD);
        request.setFullName(TestConstants.FULL_NAME);

        LoginResponse response = LoginResponse.builder()
                .accessToken(TestConstants.ACCESS_TOKEN)
                .refreshToken(TestConstants.REFRESH_TOKEN)
                .build();

        when(authService.registerUser(any(UserRequestCreate.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value(TestConstants.ACCESS_TOKEN));
    }

    @Test
    void refreshToken_Success() throws Exception {
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-access-token")
                .refreshToken(TestConstants.REFRESH_TOKEN)
                .build();

        when(authService.refreshToken(anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token/{refreshToken}", TestConstants.REFRESH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void logout_Success() throws Exception {
        doNothing().when(authService).logout();

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}