package com.estapar.parking.exception;

public class SectorFullException extends ParkingBusinessException {
    public SectorFullException(String sectorId) {
        super(String.format("Sector %s is full", sectorId));
    }
} 