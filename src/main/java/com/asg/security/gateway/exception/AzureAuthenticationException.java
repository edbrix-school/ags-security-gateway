package com.asg.security.gateway.exception;

import com.asg.security.gateway.model.AzureADResponse;

public class AzureAuthenticationException extends RuntimeException {

    private final transient AzureADResponse aadResponse;

    public AzureAuthenticationException(String message) {
        super(message);
        this.aadResponse = null;
    }

    public AzureAuthenticationException(String message, AzureADResponse aadResponse) {
        super(message);
        this.aadResponse = aadResponse;
    }

    public AzureADResponse getAadResponse() {
        return aadResponse;
    }
}

