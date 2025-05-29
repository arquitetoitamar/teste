package com.estapar.parking.exception;

public class VehicleNotFoundException extends ParkingBusinessException {
    public VehicleNotFoundException(String licensePlate) {
        super(String.format("Vehicle with license plate %s not found in parking", licensePlate));
    }
} 