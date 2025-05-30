package com.estapar.parking.service;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.GarageSectorRepository;
import com.estapar.parking.repository.ParkingSpotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarageService {
    private final GarageSectorRepository garageSectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional
    public void configureGarage(GarageConfigDTO config) {
        // Configure sectors
        List<GarageSector> sectors = config.getGarage().stream()
            .map(this::createSector)
            .toList();
        garageSectorRepository.saveAll(sectors);

        // Configure spots
        List<ParkingSpot> spots = config.getSpots().stream()
            .map(this::createSpot)
            .toList();
        parkingSpotRepository.saveAll(spots);
    }

    private GarageSector createSector(GarageConfigDTO.GarageSectorConfig config) {
        GarageSector sector = new GarageSector();
        sector.setId(config.getSector());
        sector.setBasePrice(config.getBasePrice());
        sector.setMaxCapacity(config.getMaxCapacity());
        sector.setOpenHour(LocalTime.parse(config.getOpenHour(), TIME_FORMATTER));
        sector.setCloseHour(LocalTime.parse(config.getCloseHour(), TIME_FORMATTER));
        sector.setDurationLimitMinutes(config.getDurationLimitMinutes());
        sector.setCurrentOccupancy(0);
        return sector;
    }

    private ParkingSpot createSpot(GarageConfigDTO.ParkingSpotConfig config) {
        ParkingSpot spot = new ParkingSpot();
        spot.setSectorId(config.getSector());
        spot.setLatitude(config.getLat());
        spot.setLongitude(config.getLng());
        spot.setOccupied(false);
        return spot;
    }

    @Transactional(readOnly = true)
    public List<GarageSector> getAllSectors() {
        return garageSectorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> getSpotsBySector(String sectorId) {
        return parkingSpotRepository.findBySectorIdAndOccupiedFalse(sectorId);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> getAllSpots() {
        return parkingSpotRepository.findAll();
    }

    @Transactional(readOnly = true)
    public GarageConfigDTO getCurrentConfig() {
        return new GarageConfigDTO(
            garageSectorRepository.findAll(),
            parkingSpotRepository.findAll()
        );
    }

    @Transactional
    public void importGarageConfig(GarageConfigDTO config) {
        garageSectorRepository.deleteAll();
        parkingSpotRepository.deleteAll();
        
        if (config.getGarage() != null) {
            List<GarageSector> sectors = config.getGarage().stream()
                .map(this::createSector)
                .toList();
            garageSectorRepository.saveAll(sectors);
        }
        if (config.getSpots() != null) {
            List<ParkingSpot> spots = config.getSpots().stream()
                .map(this::createSpot)
                .toList();
            parkingSpotRepository.saveAll(spots);
        }
    }
} 