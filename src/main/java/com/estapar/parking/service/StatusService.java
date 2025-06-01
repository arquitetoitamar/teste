package com.estapar.parking.service;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.exception.ResourceNotFoundException;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.repository.GarageSectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final ParkingSpotRepository spotRepository;
    private final ParkingEventRepository eventRepository;
    private final GarageSectorRepository sectorRepository;

    @Transactional(readOnly = true)
    public PlateStatusDTO getPlateStatus(String licensePlate) {
        if (licensePlate == null || licensePlate.isEmpty()) {
            return new PlateStatusDTO(licensePlate, BigDecimal.ZERO, null, LocalDateTime.now(), null, null);
        }

        Optional<ParkingSpot> spotOpt = spotRepository.findByLicensePlateAndOccupiedTrue(licensePlate);
        LocalDateTime now = LocalDateTime.now();

        if (spotOpt.isEmpty()) {
            return new PlateStatusDTO(licensePlate, BigDecimal.ZERO, null, now, null, null);
        }

        ParkingSpot spot = spotOpt.get();
        Optional<ParkingEvent> entryEvent = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(licensePlate, "ENTRY")
            .stream()
            .findFirst();

        if (entryEvent.isEmpty()) {
            return new PlateStatusDTO(
                licensePlate,
                BigDecimal.ZERO,
                null,
                now,
                spot.getLatitude(),
                spot.getLongitude()
            );
        }

        LocalDateTime entryTime = entryEvent.get().getTimestamp();
        BigDecimal priceUntilNow = calculatePrice(licensePlate, now);

        return new PlateStatusDTO(
            licensePlate,
            priceUntilNow,
            entryTime,
            now,
            spot.getLatitude(),
            spot.getLongitude()
        );
    }

    @Transactional(readOnly = true)
    public SpotStatusDTO getSpotStatus(double latitude, double longitude) {
        Optional<ParkingSpot> spotOpt = spotRepository.findByLatitudeAndLongitude(latitude, longitude);
        LocalDateTime now = LocalDateTime.now();
        
        if (spotOpt.isEmpty()) {
            return new SpotStatusDTO(false, "", BigDecimal.ZERO, null, now);
        }

        ParkingSpot spot = spotOpt.get();
        
        if (!spot.isOccupied()) {
            return new SpotStatusDTO(false, "", BigDecimal.ZERO, null, now);
        }

        Optional<ParkingEvent> entryEvent = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(spot.getLicensePlate(), "ENTRY")
            .stream()
            .findFirst();

        if (entryEvent.isEmpty()) {
            return new SpotStatusDTO(true, spot.getLicensePlate(), BigDecimal.ZERO, null, now);
        }

        LocalDateTime entryTime = entryEvent.get().getTimestamp();
        BigDecimal priceUntilNow = calculatePrice(spot.getLicensePlate(), now);

        return new SpotStatusDTO(
            true,
            spot.getLicensePlate(),
            priceUntilNow,
            entryTime,
            now
        );
    }

    private BigDecimal calculatePrice(String licensePlate, LocalDateTime exitTime) {
        Optional<ParkingEvent> entryEvent = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(licensePlate, "ENTRY")
            .stream()
            .findFirst();

        if (entryEvent.isEmpty()) {
            return BigDecimal.ZERO;
        }

        LocalDateTime entryTime = entryEvent.get().getTimestamp();
        long hours = java.time.Duration.between(entryTime, exitTime).toHours();
        long minutes = java.time.Duration.between(entryTime, exitTime).toMinutes() % 60;

        // Busca o setor para obter o preço base e calcular o preço dinâmico
        ParkingSpot spot = spotRepository.findByLicensePlateAndOccupiedTrue(licensePlate)
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "Vaga não encontrada"));
        
        GarageSector sector = sectorRepository.findById(spot.getSectorId())
            .orElseThrow(() -> new ResourceNotFoundException("GarageSector", "Setor não encontrado"));

        // Usa o preço dinâmico do setor
        BigDecimal dynamicPrice = sector.calculateDynamicPrice();
        BigDecimal totalPrice = dynamicPrice.multiply(BigDecimal.valueOf(hours));

        if (minutes > 0) {
            BigDecimal minutePrice = dynamicPrice.divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(minutePrice.multiply(BigDecimal.valueOf(minutes)));
        }

        return totalPrice.setScale(2, java.math.RoundingMode.HALF_UP);
    }
} 