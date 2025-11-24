package com.als.securityserver.util;

import com.als.securityserver.model.AuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContext {

    private UserContext() {
    }

    public static AuthenticationDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof AuthenticationDetails details) {
            return details;
        }
        return null;
    }

    public static Long getUserPoid() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getLoggedInUserPoid() : null;
    }

    public static String getUserEmail() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getLoggedInUserEmail() : null;
    }

    public static String getUserRole() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getLoggedInUserRole() : null;
    }

    public static String getUserId() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getLoggedInUserId() : null;
    }
}

