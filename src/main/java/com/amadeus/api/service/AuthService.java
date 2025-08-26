package com.amadeus.api.service;

import com.amadeus.api.dto.LoginRequest;
import com.amadeus.api.dto.LoginResponse;

public interface AuthService {
    
    LoginResponse login(LoginRequest loginRequest);
    
    void logout(String token);
}
