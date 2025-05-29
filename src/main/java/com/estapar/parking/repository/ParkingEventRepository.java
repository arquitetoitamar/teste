package com.estapar.parking.repository;

import com.estapar.parking.model.ParkingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkingEventRepository extends JpaRepository<ParkingEvent, Long> {
    List<ParkingEvent> findByLicensePlateAndTypeOrderByTimestampDesc(String licensePlate, String type);
    List<ParkingEvent> findByLicensePlateOrderByTimestampDesc(String licensePlate);
    List<ParkingEvent> findBySectorIdOrderByTimestampDesc(String sectorId); 

    @Transactional(readOnly = true)
    @Query("SELECT COALESCE(SUM(e.price), 0) FROM ParkingEvent e WHERE e.timestamp BETWEEN :start AND :end AND e.sectorId = :sector")
    BigDecimal calculateRevenueByDateAndSector(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("sector") String sector);
} 