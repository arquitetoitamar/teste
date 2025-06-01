package com.estapar.parking.service;

import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.GarageSectorRepository;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StatusServiceIntegrationTest {

    @Autowired
    private StatusService statusService;
    
    @Autowired
    private ParkingSpotRepository spotRepository;
    
    @Autowired
    private ParkingEventRepository eventRepository;
    
    @Autowired
    private GarageSectorRepository sectorRepository;

    @Test
    void getSpotStatus_WhenOccupied_ShouldReturnCorrectStatus() {
        // Arrange
        String licensePlate = "ABC1234";
        double latitude = -23.561684;
        double longitude = -46.655981;
        
        // Criar e salvar o setor primeiro
        GarageSector sector = new GarageSector();
        sector.setId("A");
        sector.setBasePrice(new BigDecimal("10.00"));
        sector.setMaxCapacity(100);
        sector.setCurrentOccupancy(0);
        sector.setOpenHour(LocalTime.of(6, 0));
        sector.setCloseHour(LocalTime.of(22, 0));
        sector.setDurationLimitMinutes(1440);
        sectorRepository.save(sector);
        
        // Criar e salvar a vaga
        ParkingSpot spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spot.setLicensePlate(licensePlate);
        spot.setOccupied(true);
        spotRepository.save(spot);
        
        // Criar evento de entrada
        ParkingEvent entryEvent = new ParkingEvent();
        entryEvent.setLicensePlate(licensePlate);
        entryEvent.setType("ENTRY");
        entryEvent.setTimestamp(LocalDateTime.now().minusHours(2));
        entryEvent.setLatitude(latitude);
        entryEvent.setLongitude(longitude);
        entryEvent.setSectorId(sector.getId());
        eventRepository.save(entryEvent);
        
        // Act
        SpotStatusDTO result = statusService.getSpotStatus(latitude, longitude);
        
        // Assert
        assertTrue(result.isOccupied());
        assertEquals(licensePlate, result.getLicensePlate());
        assertNotNull(result.getPriceUntilNow());
        assertTrue(result.getPriceUntilNow().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(result.getEntryTime());
        assertNotNull(result.getTimeParked());
    }
} 