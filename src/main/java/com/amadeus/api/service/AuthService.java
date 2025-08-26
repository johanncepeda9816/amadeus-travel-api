package com.amadeus.api.service;

import com.amadeus.api.dto.request.LoginRequest;
import com.amadeus.api.dto.response.LoginResponse;

public interface AuthService {
    
    LoginResponse login(LoginRequest loginRequest);
    
    void logout(String token);
}
