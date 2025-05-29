package com.estapar.parking.service;

import com.estapar.parking.dto.RevenueResponse;
import com.estapar.parking.repository.ParkingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RevenueServiceTest {
    @Mock
    private ParkingEventRepository eventRepository;

    private RevenueService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RevenueService(eventRepository);
    }

    @Test
    void getRevenue() {
        // Setup
        String date = "2025-01-01";
        String sector = "A";
        BigDecimal expectedAmount = BigDecimal.valueOf(30);
        LocalDateTime start = LocalDate.parse(date).atStartOfDay();
        LocalDateTime end = LocalDate.parse(date).atTime(LocalTime.MAX);

        when(eventRepository.calculateRevenueByDateAndSector(start, end, sector))
            .thenReturn(expectedAmount);

        // Test
        RevenueResponse result = service.getRevenue(date, sector);

        // Verify
        assertNotNull(result);
        assertEquals(expectedAmount, result.getAmount());
        assertEquals("BRL", result.getCurrency());
        assertNotNull(result.getTimestamp());
    }
} 