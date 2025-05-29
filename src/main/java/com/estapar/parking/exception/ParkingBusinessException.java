package com.estapar.parking.exception;

public abstract class ParkingBusinessException extends RuntimeException {
    protected ParkingBusinessException(String message) {
        super(message);
    }
} 