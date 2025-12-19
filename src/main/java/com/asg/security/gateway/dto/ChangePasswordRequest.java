package com.asg.security.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "User ID is mandatory")
    @Size(max = 20, message = "userId must not exceed 20 characters")
    private String userId;
    
    @NotBlank(message = "Old password is mandatory")
    private String oldPassword;
    
    @NotBlank(message = "New password is mandatory")
    private String newPassword;
}