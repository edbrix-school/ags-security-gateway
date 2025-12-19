package com.asg.security.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {
    @NotBlank(message = "User ID is mandatory")
    @Size(max = 20, message = "User Id must not exceed 20 characters")
    private String userId;
}