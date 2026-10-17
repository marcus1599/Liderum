package com.example.Liderum.exceptions;

public class UserStatusConflictException extends RuntimeException {
    public UserStatusConflictException() {
        super("User status transition is not available.");
    }
}
