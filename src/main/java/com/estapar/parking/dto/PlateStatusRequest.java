package com.estapar.parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PlateStatusRequest {
    @JsonProperty("license_plate")
    private String licensePlate;
} 