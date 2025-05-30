package com.estapar.parking.service;

import com.estapar.parking.dto.VehicleEventDTO;
import com.estapar.parking.exception.ResourceNotFoundException;
import com.estapar.parking.exception.SectorFullException;
import com.estapar.parking.exception.SpotOccupiedException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.GarageSectorRepository;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ParkingService {
    private final GarageSectorRepository sectorRepository;
    private final ParkingSpotRepository spotRepository;
    private final ParkingEventRepository eventRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public ParkingService(GarageSectorRepository sectorRepository, 
                         ParkingSpotRepository spotRepository,
                         ParkingEventRepository eventRepository) {
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void handleWebhookEvent(VehicleEventDTO event) {

            switch (event.getEventType()) {
                case "ENTRY":
                    handleEntryEvent(event);
                    break;
                case "PARKED":
                    handleParkedEvent(event);
                    break;
                case "EXIT":
                    handleExitEvent(event);
                    break;
                default:
                    throw new IllegalArgumentException("Evento inválido");
            }
    }
    
    private void handleEntryEvent(VehicleEventDTO event) {
        if (event.getLicensePlate() == null && event.getEntryTime() == null) {
            throw new IllegalArgumentException("Placa e data de entrada inválida");
        }
        
        // Verifica se o veículo já está no estacionamento
        if (spotRepository.findByLicensePlate(event.getLicensePlate()).isPresent()) {
            throw new VehicleAlreadyParkedException("O veículo já está no estacionamento");
        }
        
        // Verifica se já existe um evento de entrada não finalizado
        List<ParkingEvent> entryEvents = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(event.getLicensePlate(), "ENTRY");
        List<ParkingEvent> exitEvents = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(event.getLicensePlate(), "EXIT");
        
        if (!entryEvents.isEmpty() && (exitEvents.isEmpty() || 
            entryEvents.get(0).getTimestamp().isAfter(exitEvents.get(0).getTimestamp()))) {
            throw new VehicleAlreadyParkedException("O veículo já possui uma entrada registrada");
        }

        
        // Registra o evento de entrada
        ParkingEvent parkingEvent = new ParkingEvent();
        parkingEvent.setLicensePlate(event.getLicensePlate());
        parkingEvent.setType(event.getEventType());
        parkingEvent.setTimestamp(LocalDateTime.parse(event.getEntryTime(), formatter));
        if (event.getLatitude() != null && event.getLongitude() != null) {
            parkingEvent.setLatitude(event.getLatitude());
            parkingEvent.setLongitude(event.getLongitude());
        }
        
        eventRepository.save(parkingEvent);
    }
    

    
    private void handleParkedEvent(VehicleEventDTO event) {
        if (event.getLicensePlate() == null || event.getLatitude() == null || event.getLongitude() == null) {
            throw new IllegalArgumentException("Placa, latitude e longitude inválidos");
        }
        
       
            ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(event.getLatitude(), event.getLongitude())
                .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "Não foi encontrado setor para as coordenadas informadas"));
                
            if (spot.isOccupied()) {
                throw new SpotOccupiedException("A vaga já está ocupada");
            }
            
            GarageSector sector = sectorRepository.findById(spot.getSectorId())
                .orElseThrow(() -> new ResourceNotFoundException("GarageSector", "Setor não encontrado"));
                
            if (!sector.canAcceptNewVehicle()) {
                throw new SectorFullException("O setor está com capacidade máxima");
            }
            
            spot.parkVehicle(event.getLicensePlate(), LocalDateTime.now());
            sector.incrementOccupancy();
            
            ParkingEvent parkingEvent = new ParkingEvent();
            parkingEvent.setLicensePlate(event.getLicensePlate());
            parkingEvent.setType(event.getEventType());
            parkingEvent.setTimestamp(LocalDateTime.now());
            parkingEvent.setLatitude(event.getLatitude());
            parkingEvent.setLongitude(event.getLongitude());
            parkingEvent.setSectorId(sector.getId());
            
            spotRepository.save(spot);
            sectorRepository.save(sector);
            eventRepository.save(parkingEvent);
       
    }
    
    private void handleExitEvent(VehicleEventDTO event) {
        if (event.getExitTime() == null || event.getLicensePlate() == null) {
            throw new IllegalArgumentException("Data de saída ou placa inválida");
        }
        
        ParkingSpot spot = spotRepository.findByLicensePlate(event.getLicensePlate())
            .orElse(null);
            
        if (spot == null || !spot.isOccupied()) {
            throw new ResourceNotFoundException("ParkingSpot", "Não foi encontrado veiculo no estacionamento ou veiculo ja saiu");
        }
        
        GarageSector sector = sectorRepository.findById(spot.getSectorId())
            .orElseThrow(() -> new ResourceNotFoundException("GarageSector", "Setor não encontrado"));
            
        
        spot.releaseVehicle();
        sector.decrementOccupancy();
        
        ParkingEvent parkingEvent = new ParkingEvent();
        parkingEvent.setLicensePlate(event.getLicensePlate());
        parkingEvent.setType(event.getEventType());
        parkingEvent.setTimestamp(LocalDateTime.parse(event.getExitTime(), formatter));
        parkingEvent.setLatitude(spot.getLatitude());
        parkingEvent.setLongitude(spot.getLongitude());
        parkingEvent.setSectorId(sector.getId());
        
        // Calculate and set the price
        BigDecimal price = calculatePrice(event.getLicensePlate(), LocalDateTime.parse(event.getExitTime(), formatter));
        parkingEvent.setPrice(price);
        
        spotRepository.save(spot);
        sectorRepository.save(sector);
        eventRepository.save(parkingEvent);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> getAvailableSpots(String sectorId) {
        return spotRepository.findBySectorIdAndOccupiedFalse(sectorId);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpot> getOccupiedSpots(String sectorId) {
        return spotRepository.findBySectorIdAndOccupiedTrue(sectorId);
    }

    @Transactional(readOnly = true)
    public List<GarageSector> getAllSectors() {
        return sectorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ParkingSpot getSpotByCoordinates(double latitude, double longitude) {
        return spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "Não foi encontrado setor para as coordenadas informadas"));
    }

    @Transactional(readOnly = true)
    public ParkingSpot getSpotByLicensePlate(String licensePlate) {
        return spotRepository.findByLicensePlate(licensePlate)
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "license plate"));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(String licensePlate, LocalDateTime exitTime) {
        ParkingSpot spot = spotRepository.findByLicensePlate(licensePlate)
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "license plate"));
            
        GarageSector sector = sectorRepository.findById(spot.getSectorId())
            .orElseThrow(() -> new ResourceNotFoundException("GarageSector", spot.getSectorId()));

        // Busca o evento de entrada do veículo
        ParkingEvent entryEvent = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(licensePlate, "ENTRY")
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("ParkingEvent", "entry event"));

        Duration duration = Duration.between(entryEvent.getTimestamp(), exitTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        BigDecimal basePrice = sector.getBasePrice();
        BigDecimal totalPrice = basePrice.multiply(BigDecimal.valueOf(hours));

        if (minutes > 0) {
            BigDecimal minutePrice = basePrice.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(minutePrice.multiply(BigDecimal.valueOf(minutes)));
        }

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }
} 