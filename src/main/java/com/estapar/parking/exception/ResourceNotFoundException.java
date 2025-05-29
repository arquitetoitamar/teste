package com.estapar.parking.exception;

public class ResourceNotFoundException extends ParkingBusinessException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }
} 