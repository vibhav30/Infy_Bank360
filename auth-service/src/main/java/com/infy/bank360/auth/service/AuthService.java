package com.infy.bank360.auth.service;

import com.infy.bank360.auth.dto.AuthRequestDTO;
import com.infy.bank360.auth.dto.AuthResponseDTO;
import com.infy.bank360.auth.dto.RegisterRequestDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO login(AuthRequestDTO request);
    boolean validateToken(String token);
}
