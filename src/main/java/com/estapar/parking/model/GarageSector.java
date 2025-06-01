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
        
        if (occupancyRate >= 1.0) {
            return basePrice.multiply(BigDecimal.valueOf(1.25)); // 25% increase at 100%
        } else if (occupancyRate >= 0.75) {
            return basePrice.multiply(BigDecimal.valueOf(1.25)); // 25% increase at 75-100%
        } else if (occupancyRate >= 0.50) {
            return basePrice.multiply(BigDecimal.valueOf(1.10)); // 10% increase at 50-75%
        } else if (occupancyRate >= 0.25) {
            return basePrice; // Base price at 25-50%
        } else {
            return basePrice.multiply(BigDecimal.valueOf(0.90)); // 10% discount below 25%
        }
    }
} 