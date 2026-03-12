package org.example.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.BaseException;
import org.example.model.dao.Users;
import org.example.repo.user.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDetailsServiceImpl implements UserDetailsService {

    final UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Attempting to load user by username: {}", username);

        Users users = findUserByUser(username);

        log.debug("User found: id={}, username={}", users.getId(), users.getUsername());
        log.info("User {} successfully authenticated", username);

        return new UserPrincipal(users.getId(), users.getUsername(), users.getPassword());
    }

    private Users findUserByUser(String user) {

        log.debug("Searching user in database by username: {}", user);

        return userRepository.findUserByUsername(user)
                .map(u -> {
                    log.info("User '{}' found in database with id={}", user, u.getId());
                    return u;
                })
                .orElseThrow(() -> {
                    log.error("User '{}' not found in database", user);
                    return BaseException.notFound(Users.class.getSimpleName(), "username", user);
                });
    }
}