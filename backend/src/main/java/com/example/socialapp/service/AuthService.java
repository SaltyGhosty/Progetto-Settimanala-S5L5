package com.example.socialapp.service;

import com.example.socialapp.dto.AuthDtos.AuthResponse;
import com.example.socialapp.dto.AuthDtos.LoginRequest;
import com.example.socialapp.dto.AuthDtos.RegisterRequest;
import com.example.socialapp.entity.User;
import com.example.socialapp.exception.DuplicateResourceException;
import com.example.socialapp.repository.UserRepository;
import com.example.socialapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username già in uso");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email già in uso");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Utente autenticato non trovato"));

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
