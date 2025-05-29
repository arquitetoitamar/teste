package com.estapar.parking.exception;

public class SpotOccupiedException extends RuntimeException {
    public SpotOccupiedException(Long spotId) {
        super(String.format("Parking spot %d is already occupied", spotId));
    }

    public SpotOccupiedException(String spotId) {
        super(String.format("Parking spot %s is already occupied", spotId));
    }
} 