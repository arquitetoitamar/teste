package com.estapar.parking.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VehicleEventDTO implements Serializable {
    @JsonProperty("license_plate")
    private String licensePlate;
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("entry_time")
    private String entryTime;
    
    @JsonProperty("exit_time")
    private String exitTime;
    
    @JsonProperty("lat")
    private Double latitude;
    
    @JsonProperty("lng")
    private Double longitude;
    
    @JsonProperty("sector_id")
    private String sectorId;
} 