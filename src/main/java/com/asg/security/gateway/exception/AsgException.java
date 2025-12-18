package com.asg.security.gateway.exception;

import lombok.Getter;

@Getter
public class AsgException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public AsgException(String message) {
        super(message);
        this.code = 500;
    }

    public AsgException(String message, int code) {
        super(message);
        this.code = code;
    }

    public AsgException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public AsgException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}

