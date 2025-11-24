package com.als.securityserver.service;

import com.als.securityserver.aad.AzureADClient;
import com.als.securityserver.entity.User;
import com.als.securityserver.exception.AzureAuthenticationException;
import com.als.securityserver.exception.EmailNotFoundException;
import com.als.securityserver.model.AuthenticationRequest;
import com.als.securityserver.model.AuthenticationResponse;
import com.als.securityserver.model.AzureADResponse;
import com.als.securityserver.model.LoginRequest;
import com.als.securityserver.repository.UserRepository;
import com.als.securityserver.util.Base64Util;
import com.als.securityserver.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RoleService roleService;
    private final AzureADClient azureADClient;

    public AuthService(UserRepository userRepository,
                       JwtUtils jwtUtils,
                       RoleService roleService,
                       AzureADClient azureADClient) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.roleService = roleService;
        this.azureADClient = azureADClient;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        String sanitizedUserId = request.getUserId().trim();
        String password = Base64Util.decode(request.getPassword().trim());

        User user = userRepository.findByUserIdIgnoreCaseAndActive(sanitizedUserId, "Y")
                .orElseThrow(() -> new IllegalArgumentException("Incorrect credentials. Please check your userId and password and try again"));

        if ("Y".equalsIgnoreCase(user.getUserLocked())) {
            String lockReason = user.getUserLockedReason();
            String errorMessage = "User account is locked";
            if (StringUtils.isNotBlank(lockReason)) {
                errorMessage += ": " + lockReason;
            }
            throw new IllegalStateException(errorMessage);
        }

        String hashPassword = getSecureString(password, "salt");
        if (!hashPassword.equals(user.getPwd())) {
            throw new IllegalArgumentException("Incorrect credentials. Please check your userId and password and try again");
        }

        return generateAuthenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse ssoLogin(AuthenticationRequest authenticationRequest) {
        try {
            AzureADResponse azureResponse = azureADClient.validate(authenticationRequest.getUsername(),
                    authenticationRequest.getAzureAccessToken());
            if (azureResponse != null && HttpStatus.OK.value() == azureResponse.getStatusCode()) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                throw new AzureAuthenticationException("No Response from Azure");
            }
        } catch (EmailNotFoundException e) {
            throw e;
        } catch (DisabledException e) {
            throw new DisabledException(e.getMessage(), e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        } catch (AzureAuthenticationException e) {
            throw e;
        }

        User user = userRepository.findByEmailIgnoreCaseAndActive(authenticationRequest.getUsername(), "Y")
                .orElseThrow(() -> new EmailNotFoundException("Active user not found with email " + authenticationRequest.getUsername()));

        if ("Y".equalsIgnoreCase(user.getUserLocked())) {
            String lockReason = user.getUserLockedReason();
            String errorMessage = "User account is locked";
            if (StringUtils.isNotBlank(lockReason)) {
                errorMessage += ": " + lockReason;
            }
            throw new IllegalStateException(errorMessage);
        }

        return generateAuthenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(String userId) {
        User user = userRepository.findByUserIdIgnoreCaseAndActive(userId, "Y")
                .orElseThrow(() -> new IllegalArgumentException("Active user not found with userId " + userId));
        return generateAuthenticationResponse(user);
    }

    private AuthenticationResponse generateAuthenticationResponse(User user) {
        try {


        List<String> roleNames = roleService.getUserRoleNames(user.getUserPoid());
        String token = jwtUtils.generateToken(user, roleNames);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUserId());

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userName", user.getUserName());
        userDetails.put("userId", user.getUserId());
        userDetails.put("userPoid", user.getUserPoid());
        userDetails.put("userEmail", user.getEmail());
        userDetails.put("groupPoid", user.getGroupPoid());
        userDetails.put("joinedDate", user.getCreatedDate());
        userDetails.put("resetPasswordNextLogin", user.getResetPasswordNextLogin());
        userDetails.put("defaultCompanyPoid", user.getDefaultCompanyPoid());
        userDetails.put("roles", roleNames);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAuthToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserDetails(userDetails);
        response.setTokenExpiry(jwtUtils.getExpirationDate(token));
        return response;}catch (Exception e){e.printStackTrace();
        throw new RuntimeException(e)
      ;  }
    }

    public static String getSecureString(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}

