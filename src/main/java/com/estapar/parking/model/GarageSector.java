package com.estapar.parking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "garage_sectors")
public class GarageSector {
    @Id
    private String id;
    
    @Column(nullable = false)
    private BigDecimal basePrice;
    
    @Column(nullable = false)
    private int maxCapacity;
    
    @Column(nullable = false)
    private LocalTime openHour;
    
    @Column(nullable = false)
    private LocalTime closeHour;
    
    @Column(nullable = false)
    private int durationLimitMinutes;
    
    @Column(nullable = false)
    private int currentOccupancy;

    public boolean canAcceptNewVehicle() {
        return currentOccupancy < maxCapacity;
    }

    public void incrementOccupancy() {
        if (currentOccupancy >= maxCapacity) {
            throw new IllegalStateException("Sector is at maximum capacity");
        }
        currentOccupancy++;
    }

    public void decrementOccupancy() {
        if (currentOccupancy <= 0) {
            throw new IllegalStateException("Sector is already empty");
        }
        currentOccupancy--;
    }

    public BigDecimal calculateDynamicPrice() {
        double occupancyRate = (double) currentOccupancy / maxCapacity;
        if (occupancyRate >= 0.9) {
            return basePrice.multiply(BigDecimal.valueOf(1.5)); // 50% increase when almost full
        } else if (occupancyRate >= 0.7) {
            return basePrice.multiply(BigDecimal.valueOf(1.3)); // 30% increase when 70-90% full
        } else if (occupancyRate >= 0.5) {
            return basePrice.multiply(BigDecimal.valueOf(1.2)); // 20% increase when 50-70% full
        }
        return basePrice;
    }
} 