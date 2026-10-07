package com.infy.bank360.auth.service.impl;

import com.infy.bank360.auth.dto.AuthRequestDTO;
import com.infy.bank360.auth.dto.AuthResponseDTO;
import com.infy.bank360.auth.dto.RegisterRequestDTO;
import com.infy.bank360.auth.entity.User;
import com.infy.bank360.auth.repository.UserRepository;
import com.infy.bank360.auth.security.JwtTokenProvider;
import com.infy.bank360.auth.service.AuthService;
import com.infy.bank360.common.exception.InfyBankException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InfyBankException("ERR_USER_EXISTS", "Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InfyBankException("ERR_USER_EXISTS", "Email already registered: " + request.getEmail());
        }

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole() 
                : "ROLE_CUSTOMER";

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status("PENDING_KYC")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getUserId(), savedUser.getUsername(), savedUser.getRole());

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InfyBankException("ERR_INVALID_CREDENTIALS", "Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InfyBankException("ERR_INVALID_CREDENTIALS", "Invalid username or password.");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
