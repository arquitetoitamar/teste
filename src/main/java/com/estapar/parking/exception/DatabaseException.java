package com.estapar.parking.exception;

public class DatabaseException extends InfrastructureException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
} 