package com.estapar.parking.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SectorDTO {
    private String sector;
    
    @JsonProperty("base_price")
    private BigDecimal basePrice;
    
    @JsonProperty("max_capacity")
    private Integer maxCapacity;
    
    @JsonProperty("open_hour")
    private String openHour;
    
    @JsonProperty("close_hour")
    private String closeHour;
    
    @JsonProperty("duration_limit_minutes")
    private Integer durationLimitMinutes;
}
