package com.estapar.parking.exception;

public class ExternalServiceException extends InfrastructureException {
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error in service %s: %s", serviceName, message));
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error in service %s: %s", serviceName, message), cause);
    }
} 