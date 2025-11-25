package com.asg.security.gateway.model;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class AuthenticationRequest implements Serializable {

    private static final long serialVersionUID = 550269063035507976L;

    @NotBlank
    private String username;

    @NotBlank
    private String azureAccessToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAzureAccessToken() {
        return azureAccessToken;
    }

    public void setAzureAccessToken(String azureAccessToken) {
        this.azureAccessToken = azureAccessToken;
    }
}

