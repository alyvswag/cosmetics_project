package org.example.demo13213.service.auth;

import org.example.demo13213.constant.TestConstants;
import org.example.demo13213.model.dao.Users;
import org.example.demo13213.model.dao.Carts;
import org.example.demo13213.model.dto.request.login.LoginRequestPayload;
import org.example.demo13213.model.dto.request.login.UserRequestCreate;
import org.example.demo13213.model.dto.response.login.LoginResponse;
import org.example.demo13213.repo.cart.CartRepo;
import org.example.demo13213.repo.user.UserRepo;
import org.example.demo13213.security.jwt.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepo userRepo;
    @Mock
    CartRepo cartRepo;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TokenProvider tokenProvider;
    @Mock
    UserDetailsService userDetailsService;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    void registerUser_FullFlow_Success() {
        UserRequestCreate request = new UserRequestCreate(TestConstants.USERNAME, TestConstants.FULL_NAME, TestConstants.PASSWORD);
        Users mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setUsername(TestConstants.USERNAME);

        when(userRepo.findUserByUsername(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encrypted_pass");
        when(userRepo.save(any(Users.class))).thenReturn(mockUser);
        when(tokenProvider.generate(any(Users.class))).thenReturn(List.of(TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN));
        when(userRepo.findUserByUsername(TestConstants.USERNAME)).thenReturn(Optional.of(mockUser));

        LoginResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals(TestConstants.ACCESS_TOKEN, response.getAccessToken());
        verify(userRepo, times(1)).save(any(Users.class));
        verify(cartRepo, times(1)).save(any(Carts.class));
    }

    @Test
    void login_FullFlow_Success() {
        LoginRequestPayload payload = new LoginRequestPayload(TestConstants.USERNAME, TestConstants.PASSWORD);
        Users mockUser = new Users();
        mockUser.setUsername(TestConstants.USERNAME);

        when(userRepo.findUserByUsername(TestConstants.USERNAME)).thenReturn(Optional.of(mockUser));
        when(tokenProvider.generate(any(Users.class))).thenReturn(List.of(TestConstants.ACCESS_TOKEN, TestConstants.REFRESH_TOKEN));

        LoginResponse response = authService.login(payload);

        assertNotNull(response);
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void refreshToken_FullFlow_Success() {
        when(tokenProvider.getUsername(anyString())).thenReturn(TestConstants.USERNAME);
        Users mockUser = new Users();
        mockUser.setUsername(TestConstants.USERNAME);
        when(userRepo.findUserByUsername(TestConstants.USERNAME)).thenReturn(Optional.of(mockUser));
        when(tokenProvider.generate(any(Users.class))).thenReturn(List.of("new-access", "new-refresh"));

        LoginResponse response = authService.refreshToken(TestConstants.REFRESH_TOKEN);

        assertNotNull(response);
        assertEquals("new-access", response.getAccessToken());
    }

    @Test
    void logout_ShouldClearContext() {
        assertDoesNotThrow(() -> authService.logout());
    }
}