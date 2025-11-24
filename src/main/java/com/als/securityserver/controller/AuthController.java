package com.als.securityserver.controller;

import com.als.securityserver.exception.AzureAuthenticationException;
import com.als.securityserver.model.AuthenticationRequest;
import com.als.securityserver.model.AuthenticationResponse;
import com.als.securityserver.model.AzureADResponse;
import com.als.securityserver.model.LoginRequest;
import com.als.securityserver.service.AuthService;
import com.als.securityserver.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return ApiResponse.success("User logged in successfully", response);
        } catch (Exception ex) {
            return ApiResponse.internalServerError(ex.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("TOKEN_USER_ID");
            AuthenticationResponse response = authService.refreshToken(userId);
            return ApiResponse.success("Token refreshed successfully", response);
        } catch (Exception ex) {
            return ApiResponse.internalServerError("Error refreshing token: " + ex.getMessage());
        }
    }

    @PostMapping("/sso")
    public ResponseEntity<?> ssoLogin(@Valid @RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        try {
            AuthenticationResponse response = authService.ssoLogin(authenticationRequest);
            return ApiResponse.success("User logged in successfully", response);
        } catch (AzureAuthenticationException ex) {
            AzureADResponse aadResponse = ex.getAadResponse();
            if (aadResponse != null) {
                request.setAttribute("AzureAuth_Response", aadResponse);
            }
            return ApiResponse.unauthorized(ex.getMessage());
        } catch (Exception ex) {
            return ApiResponse.internalServerError("Login using SSO failed: " + ex.getMessage());
        }
    }
}

