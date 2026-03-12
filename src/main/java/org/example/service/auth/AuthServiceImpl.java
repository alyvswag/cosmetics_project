package org.example.service.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.BaseException;
import org.example.model.dao.Carts;
import org.example.model.dao.Users;
import org.example.model.dto.request.login.LoginRequestPayload;
import org.example.model.dto.request.login.UserRequestCreate;
import org.example.model.dto.response.login.LoginResponse;
import org.example.repo.cart.CartRepo;
import org.example.repo.user.UserRepo;
import org.example.security.jwt.TokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

import static org.example.model.dto.enums.response.ErrorResponseMessages.INVALID_USERNAME_OR_PASSWORD;
import static org.example.model.dto.enums.response.ErrorResponseMessages.USERNAME_ALREADY_REGISTERED;
import static org.example.utils.CommonUtils.throwIf;

/* İstifadəçilərin qeydiyyatı, autentifikasiyası və token idarəetməsini
 həyata keçirən əsas təhlükəsizlik service-i. */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    final AuthenticationManager authenticationManager;
    final UserRepo userRepo;
    final CartRepo cartRepo;
    final PasswordEncoder passwordEncoder;
    final TokenProvider tokenProvider;
    final UserDetailsService userDetailsService;

    // Yeni istifadəçi yaradır və ona boş səbət təyin edir
    @Override
    public LoginResponse registerUser(UserRequestCreate userRequestCreate) {
        log.info("Starting registration process for username: {}", userRequestCreate.getUsername());

        // İstifadəçi adının bazada mövcudluğunu yoxlayır (təkrarlanma olmamalıdır)
        throwIf(
                () -> userRepo.findUserByUsername(userRequestCreate.getUsername()).isPresent(),
                BaseException.of(USERNAME_ALREADY_REGISTERED)
        );

        // Yeni istifadəçi obyekti yaradılır, şifrə kodlaşdırılır və yadda saxlanılır
        Users users = new Users();
        users.setUsername(userRequestCreate.getUsername());
        users.setPassword(passwordEncoder.encode(userRequestCreate.getPassword()));
        users.setFullName(userRequestCreate.getFullName());
        users.setIsActive(true);
        userRepo.save(users);

        // İstifadəçiyə aid boş səbət (cart) obyekti yaradılır
        Carts carts = new Carts();
        carts.setUser(users);
        cartRepo.save(carts);

        // Qeydiyyatdan dərhal sonra tokenləri hazırlayıb qaytarır
        return prepareLoginResponse(userRequestCreate.getUsername());
    }

    // İstifadəçi məlumatlarını yoxlayır və sistemə girişini təmin edir
    @Override
    public LoginResponse login(LoginRequestPayload payload) {
        log.info("Login attempt initiated for user: {}", payload.getUsername());

        // Kimlik doğrulaması aparılır (şifrə və istifadəçi adı yoxlanılır)
        authenticate(payload);

        // Giriş uğurludursa tokenləri hazırlayır
        return prepareLoginResponse(payload.getUsername());
    }

    // Mövcud refresh token ilə yeni access və refresh token cütlüyü yaradır
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Processing refresh token request");

        // Token daxilindən istifadəçi adı əldə edilir
        String username = tokenProvider.getUsername(refreshToken);

        // Yeni tokenlər generasiya olunur
        return prepareLoginResponse(username);
    }

    // Cari sessiyanı (SecurityContext) təmizləyərək çıxış edir
    @Override
    public void logout() {
        log.info("Processing logout request");

        // Təhlükəsizlik kontekstini (cari sessiyanı) sıfırlayır
        SecurityContextHolder.clearContext();
    }

    // Daxil olan token əsasında istifadəçi kimliyini SecurityContext-də saxlayır
    @Override
    public void setAuthentication(String username) {
        log.info("Setting security context for user: {}", username);

        // İstifadəçi məlumatlarını yükləyir
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Autentifikasiya obyektini SecurityContext-ə daxil edir
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    // Tokenlərin generasiya edilməsi və cavabın hazırlanması
    private LoginResponse prepareLoginResponse(String username) {
        log.info("Generating security tokens for user: {}", username);

        // İstifadəçini bazadan tapır
        Users users = findUserByUser(username);

        // TokenProvider vasitəsilə access və refresh tokenləri çəkir (list indeksləri ilə)
        String accessToken = tokenProvider.generate(users).get(0);
        String refreshToken = tokenProvider.generate(users).get(1);

        // Cavab obyektini inşa edir
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // İstifadəçini bazadan axtarır, tapmadıqda xəta qaytarır
    private Users findUserByUser(String u) {
        return userRepo.findUserByUsername(u)
                .orElseThrow(() -> {
                    log.error("User not found in database: {}", u);
                    return BaseException.notFound(Users.class.getSimpleName(), "user", u);
                });
    }

    // Şifrə və istifadəçi adının doğruluğunu yoxlayır
    private void authenticate(LoginRequestPayload request) {
        try {
            // Spring Security vasitəsilə girişi yoxlayır
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());

            // Xətanın növünə görə uyğun exception edir
            throw e.getCause() instanceof BaseException ?
                    (BaseException) e.getCause() :
                    BaseException.of(INVALID_USERNAME_OR_PASSWORD);
        }
    }
}