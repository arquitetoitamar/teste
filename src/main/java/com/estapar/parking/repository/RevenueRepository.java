package com.estapar.parking.repository;

import com.estapar.parking.model.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, String> {
    List<Revenue> findBySectorIdAndTimestampBetween(String sectorId, LocalDateTime start, LocalDateTime end);
} 