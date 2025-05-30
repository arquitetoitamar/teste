package com.estapar.parking.service;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
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

    @Transactional(readOnly = true)
    public PlateStatusDTO getPlateStatus(String licensePlate) {
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

        // Preço base por hora (pode ser ajustado conforme necessário)
        BigDecimal basePrice = new BigDecimal("10.00");
        BigDecimal totalPrice = basePrice.multiply(BigDecimal.valueOf(hours));

        if (minutes > 0) {
            BigDecimal minutePrice = basePrice.divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(minutePrice.multiply(BigDecimal.valueOf(minutes)));
        }

        return totalPrice.setScale(2, java.math.RoundingMode.HALF_UP);
    }
} 