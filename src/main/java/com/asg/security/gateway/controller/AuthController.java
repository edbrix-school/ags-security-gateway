package com.asg.security.gateway.controller;

import com.asg.security.gateway.dto.*;
import com.asg.security.gateway.entity.Company;
import com.asg.security.gateway.exception.AsgException;
import com.asg.security.gateway.exception.AzureAuthenticationException;
import com.asg.security.gateway.model.*;
import com.asg.security.gateway.service.AuthService;
import com.asg.security.gateway.service.PermissionService;
import com.asg.security.gateway.util.ApiResponse;
import com.asg.security.gateway.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asg.security.gateway.util.ApiResponse.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PermissionService permissionService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return success("User logged in successfully", response);
        } catch (Exception ex) {
            return internalServerError(ex.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("TOKEN_USER_ID");
            AuthenticationResponse response = authService.refreshToken(userId);
            return success("Token refreshed successfully", response);
        } catch (Exception ex) {
            return internalServerError("Error refreshing token: " + ex.getMessage());
        }
    }

    @PostMapping("/sso")
    public ResponseEntity<?> ssoLogin(@Valid @RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) {
        try {
            AuthenticationResponse response = authService.ssoLogin(authenticationRequest);
            return success("User logged in successfully", response);
        } catch (AzureAuthenticationException ex) {
            AzureADResponse aadResponse = ex.getAadResponse();
            if (aadResponse != null) {
                request.setAttribute("AzureAuth_Response", aadResponse);
            }
            return ApiResponse.unauthorized(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Login using SSO failed: " + ex.getMessage());
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String result = authService.changeUserPassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
            if (StringUtils.isNotBlank(result) && result.toUpperCase().contains("SUCCESS")) {
                return success("Password changed successfully", null);
            }

            return badRequest("Password change failed: " + result);

        } catch (AsgException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error changing password for userId {}: {}", request.getUserId(), e.getMessage());
            return internalServerError("Failed to change password: " + e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            String result = authService.sendOtp(request.getUserId());
            if (!StringUtils.isBlank(result) && result.toUpperCase().contains("TRUE")) {
                log.info("OTP sent successfully for userId: {}", request.getUserId());
                return success("OTP sent successfully", null);
            } else {
                log.warn("OTP sending failed for userId: {} | Result: {}", request.getUserId(), result);
                throw new AsgException("Failed to send OTP: " + result, 400);
            }
        } catch (AsgException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error sending OTP for userId {}: {}", request.getUserId(), e.getMessage());
            return internalServerError("Failed to send OTP: " + e.getMessage());
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String result = authService.forgotPassword(request.getUserId(), request.getOtp());
            if (!StringUtils.isBlank(result) && result.toUpperCase().contains("TRUE")) {
                log.info("Password reset successful for userId: {}", request.getUserId());
                return success("Password reset successful. Temporary password sent to your email.", null);
            } else {
                log.warn("Password reset failed for userId: {} | Result: {}", request.getUserId(), result);
                throw new AsgException("Password reset failed: " + result, 400);
            }
        } catch (AsgException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error resetting password for userId {}: {}", request.getUserId(), e.getMessage());
            return internalServerError("Failed to reset password: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Get userPoid from SecurityContext
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            AuthenticationDetails authDetails = (AuthenticationDetails) auth.getDetails();
            Long userPoid = authDetails.getLoggedInUserPoid();

            int clearedDrafts = authService.clearAllDraftsByUser(userPoid);
            log.info("User {} logged out. Cleared {} drafts", userPoid, clearedDrafts);
            return success("Logout successful. Cleared " + clearedDrafts + " drafts.", null);
        } catch (Exception ex) {
            log.error("Logout failed: {}", ex.getMessage());
            return internalServerError("Logout failed: " + ex.getMessage());
        }
    }

    @GetMapping("/company")
    public ResponseEntity<?> getUserCompanies(@RequestParam(required = false) Long userPoid) {
        try {
            List<Company> companies = authService.getCompanies(userPoid);
            return success("Company list fetched successfully", companies);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch company list: " + ex.getMessage());
        }
    }

    @GetMapping("/menu")
    public ResponseEntity<?> getUserMenus(@RequestParam(required = false) Long userPoid) {
        try {
            List<MenuItemDto> userMenuList = authService.getUserMenu(userPoid);
            String userId = UserContext.getUserId();
            List<PermissionDto> permissions = permissionService.getUserPermissions(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("menus", userMenuList);
            response.put("permissions", permissions);

            return success("User menu and permissions fetched successfully", response);
        } catch (Exception ex) {
            return internalServerError("Unable to fetch user menu and permissions: " + ex.getMessage());
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return unauthorized("Unauthorized to get permissions");
            }

            String userId = UserContext.getUserId() != null ? UserContext.getUserId() : null;

            List<PermissionDto> permissions = permissionService.getUserPermissions(userId);

            return success("Permissions for this user fetched successfully.", new UserPermissionsResponse(userId, permissions));
        } catch (Exception ex) {
            return internalServerError("Failed to fetch permissions ->  " + ex.getMessage());
        }
    }

}

