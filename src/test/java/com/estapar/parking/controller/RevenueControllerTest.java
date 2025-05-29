package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueRequest;
import com.estapar.parking.dto.RevenueResponse;
import com.estapar.parking.service.RevenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueControllerTest {

    @Mock
    private RevenueService revenueService;

    @InjectMocks
    private RevenueController controller;

    private RevenueResponse revenueResponse;
    private RevenueRequest revenueRequest;
    private static final String DATE = "2024-03-20";
    private static final String SECTOR = "A1";

    @BeforeEach
    void setUp() {
        // Setup request
        revenueRequest = new RevenueRequest();
        revenueRequest.setDate(DATE);
        revenueRequest.setSector(SECTOR);

        // Setup response
        revenueResponse = new RevenueResponse();
        revenueResponse.setAmount(BigDecimal.valueOf(1000));
        revenueResponse.setCurrency("BRL");
        revenueResponse.setTimestamp(Instant.now());
    }

    @Test
    void getRevenue_ShouldReturnRevenue() {
        // Setup
        when(revenueService.getRevenue(DATE, SECTOR)).thenReturn(revenueResponse);

        // Test
        ResponseEntity<RevenueResponse> response = controller.getRevenue(revenueRequest);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(revenueResponse, response.getBody());
        verify(revenueService, times(1)).getRevenue(DATE, SECTOR);
    }

    @Test
    void getRevenue_WhenDateIsInvalid_ShouldReturnEmptyRevenue() {
        // Setup
        revenueRequest.setDate("invalid-date");
        RevenueResponse emptyRevenue = new RevenueResponse();
        emptyRevenue.setAmount(BigDecimal.ZERO);
        emptyRevenue.setCurrency("BRL");
        emptyRevenue.setTimestamp(Instant.now());
        
        when(revenueService.getRevenue("invalid-date", SECTOR)).thenReturn(emptyRevenue);

        // Test
        ResponseEntity<RevenueResponse> response = controller.getRevenue(revenueRequest);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(emptyRevenue, response.getBody());
        verify(revenueService, times(1)).getRevenue("invalid-date", SECTOR);
    }

    @Test
    void getRevenue_WhenSectorIsNull_ShouldReturnEmptyRevenue() {
        // Setup
        revenueRequest.setSector(null);
        RevenueResponse emptyRevenue = new RevenueResponse();
        emptyRevenue.setAmount(BigDecimal.ZERO);
        emptyRevenue.setCurrency("BRL");
        emptyRevenue.setTimestamp(Instant.now());
        
        when(revenueService.getRevenue(DATE, null)).thenReturn(emptyRevenue);

        // Test
        ResponseEntity<RevenueResponse> response = controller.getRevenue(revenueRequest);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(emptyRevenue, response.getBody());
        verify(revenueService, times(1)).getRevenue(DATE, null);
    }
} 