package com.asg.security.gateway.util;

import com.asg.security.gateway.model.AuthenticationDetails;
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

    public static String getUserName() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getLoggedInUserName() : null;
    }

    public static Long getGroupPoid() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getGroupPoid() : null;
    }

    public static Long getCompanyPoid() {
        AuthenticationDetails details = getCurrentUser();
        return details != null ? details.getCompanyPoid() : null;
    }
}

