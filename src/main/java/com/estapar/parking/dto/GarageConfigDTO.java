package com.estapar.parking.dto;

import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingSpot;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class GarageConfigDTO {
    private List<GarageSectorConfig> garage;
    private List<ParkingSpotConfig> spots;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Data
    public static class GarageSectorConfig {
        private String sector;
        private BigDecimal basePrice;
        private int maxCapacity;
        private String openHour;
        private String closeHour;
        private int durationLimitMinutes;
    }

    @Data
    public static class ParkingSpotConfig implements Serializable {
        private String sector;
        private double lat;
        private double lng;
    }

    public GarageConfigDTO(List<GarageSector> sectors, List<ParkingSpot> spots) {
        this.garage = sectors.stream()
            .map(sector -> {
                GarageSectorConfig config = new GarageSectorConfig();
                config.setSector(sector.getId());
                config.setBasePrice(sector.getBasePrice());
                config.setMaxCapacity(sector.getMaxCapacity());
                config.setOpenHour(sector.getOpenHour().format(TIME_FORMATTER));
                config.setCloseHour(sector.getCloseHour().format(TIME_FORMATTER));
                config.setDurationLimitMinutes(sector.getDurationLimitMinutes());
                return config;
            })
            .toList();

        this.spots = spots.stream()
            .map(spot -> {
                ParkingSpotConfig config = new ParkingSpotConfig();
                config.setSector(spot.getSectorId());
                config.setLat(spot.getLatitude());
                config.setLng(spot.getLongitude());
                return config;
            })
            .toList();
    }

    public GarageConfigDTO() {
    }
} 