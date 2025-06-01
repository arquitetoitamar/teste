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
import java.util.logging.Logger;

@Service
public class ParkingService {
    private final Logger logger = Logger.getLogger(ParkingService.class.getName());
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
        if (spotRepository.findByLicensePlateAndOccupiedTrue(event.getLicensePlate()).isPresent()) {
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
        
        logger.info("handleParkedEvent - Placa: " + event.getLicensePlate() + 
                   ", Latitude: " + event.getLatitude() + 
                   ", Longitude: " + event.getLongitude());
       
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(event.getLatitude(), event.getLongitude())
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "Não foi encontrado setor para as coordenadas informadas"));
                
        if (spot.isOccupied()) {
            throw new SpotOccupiedException("A vaga já está ocupada");
        }
        
        GarageSector sector = sectorRepository.findById(spot.getSectorId())
            .orElseThrow(() -> new ResourceNotFoundException("GarageSector", "Setor não encontrado"));
                
        if (!sector.canAcceptNewVehicle()) {
            throw new SectorFullException("O setor está com capacidade máxima e está fechado para novas entradas");
        }
        
        // Verifica se o setor está fechado (100% de lotação)
        if (sector.getCurrentOccupancy() >= sector.getMaxCapacity()) {
            throw new SectorFullException("O setor está fechado por atingir 100% de lotação");
        }
        
        logger.info("Estacionando veículo - Placa: " + event.getLicensePlate() + 
                   ", Setor: " + sector.getId() + 
                   ", Vaga ID: " + spot.getId());
        
        spot.parkVehicle(event.getLicensePlate(), LocalDateTime.now());
        sector.incrementOccupancy();
        
        ParkingEvent parkingEvent = new ParkingEvent();
        parkingEvent.setLicensePlate(event.getLicensePlate().toUpperCase());
        parkingEvent.setType(event.getEventType());
        parkingEvent.setTimestamp(LocalDateTime.now());
        parkingEvent.setLatitude(event.getLatitude());
        parkingEvent.setLongitude(event.getLongitude());
        parkingEvent.setSectorId(sector.getId());
        
        // Calcula o preço dinâmico baseado na lotação
        BigDecimal dynamicPrice = sector.calculateDynamicPrice();
        parkingEvent.setPrice(dynamicPrice);
        
        spotRepository.save(spot);
        sectorRepository.save(sector);
        eventRepository.save(parkingEvent);
    }
    
    private void handleExitEvent(VehicleEventDTO event) {
        logger.info("handleExitEvent: " + event.getLicensePlate());
        if (event.getExitTime() == null || event.getLicensePlate() == null || event.getLicensePlate().isEmpty()) {
            throw new IllegalArgumentException("Data de saída ou placa inválida");
        }
        
        // Log para debug
        logger.info("Buscando vaga para placa: " + event.getLicensePlate());
        List<ParkingSpot> allSpots = spotRepository.findByLicensePlate(event.getLicensePlate());
        logger.info("Total de vagas encontradas: " + allSpots.size());
        for (ParkingSpot spot : allSpots) {
            logger.info("Vaga encontrada - ID: " + spot.getId() + 
                       ", Placa: " + spot.getLicensePlate() + 
                       ", Ocupada: " + spot.isOccupied() + 
                       ", Setor: " + spot.getSectorId());
        }
        
        ParkingSpot spot = spotRepository.findByLicensePlateAndOccupiedTrue(event.getLicensePlate())
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "Não foi encontrado veiculo no estacionamento ou veiculo ja saiu"));
        
        GarageSector sector = sectorRepository.findById(spot.getSectorId().toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("GarageSector", "Setor não encontrado"));
            
        // Busca o evento de entrada
        ParkingEvent entryEvent = eventRepository.findByLicensePlateAndTypeOrderByTimestampDesc(event.getLicensePlate(), "ENTRY")
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("ParkingEvent", "Evento de entrada não encontrado"));
            
        logger.info("Evento de entrada encontrado - Timestamp: " + entryEvent.getTimestamp());
        
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
        BigDecimal price = sector.calculateDynamicPrice();
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
        return spotRepository.findByLicensePlateAndOccupiedTrue(licensePlate)
            .orElseThrow(() -> new ResourceNotFoundException("ParkingSpot", "license plate"));
    }

} 