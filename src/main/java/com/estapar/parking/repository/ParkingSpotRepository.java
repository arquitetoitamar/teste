package com.estapar.parking.repository;

import com.estapar.parking.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    @Query("SELECT p FROM ParkingSpot p WHERE p.licensePlate = :licensePlate AND p.occupied = true")
    Optional<ParkingSpot> findByLicensePlateAndOccupiedTrue(@Param("licensePlate") String licensePlate);
    
    List<ParkingSpot> findByLicensePlate(String licensePlate);
    Optional<ParkingSpot> findByLatitudeAndLongitude(double latitude, double longitude);
    List<ParkingSpot> findBySectorIdAndOccupiedFalse(String sectorId);
    List<ParkingSpot> findBySectorIdAndOccupiedTrue(String sectorId);
    Optional<ParkingSpot> findFirstBySectorIdAndOccupiedFalse(String sectorId);

    @Query("SELECT p FROM ParkingSpot p WHERE p.sectorId = :sectorId AND p.entryTime = :entryTime")
    Optional<ParkingSpot> findBySectorIdAndEntryTime(@Param("sectorId") String sectorId, @Param("entryTime") LocalDateTime entryTime);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM ParkingSpot p WHERE p.sectorId = :sectorId AND p.entryTime = :entryTime")
    boolean existsBySectorIdAndEntryTime(@Param("sectorId") String sectorId, @Param("entryTime") LocalDateTime entryTime);
} 