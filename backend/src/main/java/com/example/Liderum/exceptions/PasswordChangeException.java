package com.example.Liderum.exceptions;

public class PasswordChangeException extends RuntimeException {

    public enum Code {
        CURRENT_PASSWORD_INVALID,
        PASSWORD_UNCHANGED,
        INVALID_PASSWORD_REQUEST,
        SESSION_EXPIRED
    }

    private final Code code;

    public PasswordChangeException(Code code) {
        super("Password change request rejected");
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
