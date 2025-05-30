package com.estapar.parking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "parking_spots", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"license_plate", "entry_time"}),
           @UniqueConstraint(columnNames = {"latitude", "longitude", "entry_time"})
       })
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sector_id", nullable = false)
    private String sectorId;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    @Column(name = "license_plate")
    private String licensePlate;
    
    @Column(name = "entry_time")
    private LocalDateTime entryTime;
    
    @Column(nullable = false)
    private boolean occupied;

    @PrePersist
    @PreUpdate
    public void updateOccupiedState() {
        this.occupied = licensePlate != null;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public boolean canAcceptVehicle() {
        return !occupied;
    }

    public void parkVehicle(String licensePlate, LocalDateTime entryTime) {
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.occupied = true;
    }

    public void releaseVehicle() {
        this.licensePlate = null;
        this.entryTime = null;
        this.occupied = false;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
} 