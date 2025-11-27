package com.asg.security.gateway.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "UserId is required")
    @Size(min = 1, max = 24, message = "UserId must be between 1 and 24 characters")
    @Pattern(regexp = "^[^&:;%',?+\"]*$", message = "UserId should not contain &:;%',?+\" special characters")
    private String userId;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 24, message = "Password must be between 1 and 24 characters")
    @Pattern(regexp = "^[^&:;%',?+\"]*$", message = "Password should not contain &:;%',?+\" special characters")
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

