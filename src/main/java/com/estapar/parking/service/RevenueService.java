package com.estapar.parking.service;

import com.estapar.parking.dto.RevenueResponse;
import com.estapar.parking.repository.ParkingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RevenueService {
    private final ParkingEventRepository eventRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    @Transactional(readOnly = true)
    public RevenueResponse getRevenue(String dateStr, String sector) {
        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        
        BigDecimal amount = eventRepository.calculateRevenueByDateAndSector(start, end, sector);
        
        RevenueResponse response = new RevenueResponse();
        response.setAmount(amount);
        response.setCurrency("BRL");
        response.setTimestamp(Instant.now());
        
        return response;
    }
} 