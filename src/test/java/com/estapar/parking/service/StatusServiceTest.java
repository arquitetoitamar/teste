package com.estapar.parking.service;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private ParkingEventRepository eventRepository;

    @Mock
    private ParkingSpotRepository spotRepository;

    @InjectMocks
    private StatusService service;

    private GarageSector sector;
    private ParkingSpot spot;
    private ParkingEvent event;
    private static final String LICENSE_PLATE = "ABC1234";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final BigDecimal BASE_PRICE = new BigDecimal("10.00");

    @BeforeEach
    void setUp() {
        // Setup sector
        sector = new GarageSector();
        sector.setId("A1");
        sector.setBasePrice(BASE_PRICE);
        sector.setMaxCapacity(10);
        sector.setCurrentOccupancy(1);

        // Setup spot
        spot = new ParkingSpot();
        spot.setId(1L);
        spot.setSectorId(sector.getId());
        spot.setLatitude(-23.561684);
        spot.setLongitude(-46.655981);
        spot.setLicensePlate(LICENSE_PLATE);
        spot.setOccupied(true);

        // Setup event
        event = new ParkingEvent();
        event.setLicensePlate(LICENSE_PLATE);
        event.setType("ENTRY");
        event.setTimestamp(NOW.minusHours(2));
        event.setLatitude(spot.getLatitude());
        event.setLongitude(spot.getLongitude());
        event.setSectorId(sector.getId());
    }

    @Test
    void getPlateStatus_WhenVehicleIsParked_ShouldReturnCorrectStatus() {
        // Setup
        when(spotRepository.findByLicensePlate(LICENSE_PLATE))
                .thenReturn(Optional.of(spot));

        // Test
        PlateStatusDTO status = service.getPlateStatus(LICENSE_PLATE);

        // Verify
        assertNotNull(status);
        assertEquals(LICENSE_PLATE, status.getLicensePlate());
    }

    @Test
    void getPlateStatus_WhenVehicleIsNotParked_ShouldReturnNotParked() {
        // Setup
        when(spotRepository.findByLicensePlate(LICENSE_PLATE))
                .thenReturn(Optional.empty());

        // Test
        PlateStatusDTO status = service.getPlateStatus(LICENSE_PLATE);

        // Verify
        assertNotNull(status);
        assertEquals(LICENSE_PLATE, status.getLicensePlate());
    }

    @Test
    void getPlateStatus_WhenLicensePlateIsNull_ShouldReturnNotParked() {
        // Test
        PlateStatusDTO status = service.getPlateStatus(null);

        // Verify
        assertNotNull(status);
        assertNull(status.getLicensePlate());
    }

    @Test
    void getPlateStatus_WhenLicensePlateIsEmpty_ShouldReturnNotParked() {
        // Test
        PlateStatusDTO status = service.getPlateStatus("");

        // Verify
        assertNotNull(status);
        assertEquals("", status.getLicensePlate());

    }

    @Test
    void getSpotStatus_WhenOccupied_ShouldReturnCorrectStatus() {
        // Setup
        when(spotRepository.findByLatitudeAndLongitude(spot.getLatitude(), spot.getLongitude()))
                .thenReturn(Optional.of(spot));
        when(eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(LICENSE_PLATE, "ENTRY"))
                .thenReturn(Arrays.asList(event));

        // Test
        SpotStatusDTO status = service.getSpotStatus(spot.getLatitude(), spot.getLongitude());

        // Verify
        assertNotNull(status);
        assertTrue(status.isOccupied());
        assertEquals(LICENSE_PLATE, status.getLicensePlate());
        assertEquals(NOW.minusHours(2), status.getEntryTime());
        assertEquals(new BigDecimal("20.00"), status.getPriceUntilNow());
        assertNotNull(status.getTimeParked());
    }



    @Test
    void getSpotStatus_WhenNoEntryEvent_ShouldReturnOccupiedWithoutPrice() {
        // Setup
        when(spotRepository.findByLatitudeAndLongitude(spot.getLatitude(), spot.getLongitude()))
                .thenReturn(Optional.of(spot));
        when(eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(anyString(), anyString()))
                .thenReturn(Arrays.asList());

        // Test
        SpotStatusDTO status = service.getSpotStatus(spot.getLatitude(), spot.getLongitude());

        // Verify
        assertNotNull(status);
        assertTrue(status.isOccupied());
        assertEquals(LICENSE_PLATE, status.getLicensePlate());
        assertEquals(BigDecimal.ZERO, status.getPriceUntilNow());
        assertNull(status.getEntryTime());
        assertNotNull(status.getTimeParked());
    }
} 