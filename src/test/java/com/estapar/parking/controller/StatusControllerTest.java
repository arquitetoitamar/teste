package com.estapar.parking.controller;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.PlateStatusRequest;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.dto.SpotStatusRequest;
import com.estapar.parking.service.StatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusService statusService;

    @InjectMocks
    private StatusController controller;

    private PlateStatusDTO plateStatusDTO;
    private SpotStatusDTO spotStatusDTO;
    private PlateStatusRequest plateStatusRequest;
    private SpotStatusRequest spotStatusRequest;
    private static final String LICENSE_PLATE = "ABC1234";
    private static final double LAT = -23.561684;
    private static final double LNG = -46.655981;

    @BeforeEach
    void setUp() {
        // Setup plate status request
        plateStatusRequest = new PlateStatusRequest();
        plateStatusRequest.setLicensePlate(LICENSE_PLATE);

        // Setup spot status request
        spotStatusRequest = new SpotStatusRequest();
        spotStatusRequest.setLat(LAT);
        spotStatusRequest.setLng(LNG);

        // Setup plate status response
        plateStatusDTO = new PlateStatusDTO();
        plateStatusDTO.setLicensePlate(LICENSE_PLATE);
        plateStatusDTO.setPriceUntilNow(BigDecimal.valueOf(10.00));
        plateStatusDTO.setTimeParked(LocalDateTime.now());
        plateStatusDTO.setLat(LAT);
        plateStatusDTO.setLng(LNG);

        // Setup spot status response
        spotStatusDTO = new SpotStatusDTO();
        spotStatusDTO.setPriceUntilNow(BigDecimal.valueOf(10.00));
        spotStatusDTO.setTimeParked(LocalDateTime.now());
    }

    @Test
    void getPlateStatus_ShouldReturnPlateStatus() {
        // Setup
        when(statusService.getPlateStatus(LICENSE_PLATE)).thenReturn(plateStatusDTO);

        // Test
        ResponseEntity<PlateStatusDTO> response = controller.getPlateStatus(plateStatusRequest);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(plateStatusDTO, response.getBody());
        verify(statusService, times(1)).getPlateStatus(LICENSE_PLATE);
    }

    @Test
    void getSpotStatus_ShouldReturnSpotStatus() {
        // Setup
        when(statusService.getSpotStatus(LAT, LNG)).thenReturn(spotStatusDTO);

        // Test
        ResponseEntity<SpotStatusDTO> response = controller.getSpotStatus(spotStatusRequest);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(spotStatusDTO, response.getBody());
        verify(statusService, times(1)).getSpotStatus(LAT, LNG);
    }
} 