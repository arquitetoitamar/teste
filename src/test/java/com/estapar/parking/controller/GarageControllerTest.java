package com.estapar.parking.controller;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.service.GarageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarageControllerTest {

    @Mock
    private GarageService garageService;

    @InjectMocks
    private GarageController controller;

    private GarageConfigDTO configDTO;
    private GarageSector sector;
    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        // Setup sector
        sector = new GarageSector();
        sector.setId("A1");
        sector.setBasePrice(new BigDecimal("10.00"));
        sector.setMaxCapacity(10);
        sector.setCurrentOccupancy(0);
        sector.setOpenHour(LocalTime.of(8, 0));
        sector.setCloseHour(LocalTime.of(18, 0));
        sector.setDurationLimitMinutes(120);

        // Setup spot
        spot = new ParkingSpot();
        spot.setId(1L);
        spot.setSectorId(sector.getId());
        spot.setLatitude(-23.561684);
        spot.setLongitude(-46.655981);
        spot.setOccupied(false);

        // Setup config DTO
        configDTO = new GarageConfigDTO(Arrays.asList(sector), Arrays.asList(spot));
    }

    @Test
    void getCurrentConfig_ShouldReturnCurrentConfig() {
        // Setup
        when(garageService.getCurrentConfig()).thenReturn(configDTO);

        // Test
        ResponseEntity<GarageConfigDTO> response = controller.getCurrentConfig();

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(configDTO, response.getBody());
        verify(garageService, times(1)).getCurrentConfig();
    }
} 