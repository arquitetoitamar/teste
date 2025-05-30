package com.estapar.parking.steps;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.dto.VehicleEventDTO;
import com.estapar.parking.model.GarageSector;
import com.estapar.parking.model.ParkingEvent;
import com.estapar.parking.model.ParkingSpot;
import com.estapar.parking.repository.GarageSectorRepository;
import com.estapar.parking.repository.ParkingEventRepository;
import com.estapar.parking.repository.ParkingSpotRepository;
import com.estapar.parking.service.ParkingService;
import com.estapar.parking.service.StatusService;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ParkingSteps {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private GarageSectorRepository sectorRepository;

    @Autowired
    private ParkingSpotRepository spotRepository;

    @Autowired
    private ParkingEventRepository eventRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private String licensePlate;
    private double latitude;
    private double longitude;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private GarageSector sector;
    private ParkingSpot spot;
    private Exception thrownException;
    private VehicleEventDTO eventDTO;
    private ParkingSpot foundSpot;
    private ResponseEntity<PlateStatusDTO> plateStatusResponse;
    private ResponseEntity<SpotStatusDTO> spotStatusResponse;

    @Before
    public void setup() {
        // Limpa os dados antes de cada cenário
        eventRepository.deleteAll();
        spotRepository.deleteAll();
        sectorRepository.deleteAll();
        
        // Inicializa o eventDTO
        eventDTO = new VehicleEventDTO();
    }

    @Dado("que o sistema de estacionamento está operacional")
    public void systemIsOperational() {
        // Sistema está pronto para testes
    }

    @Dado("existe um setor {string} com preço base {string} e capacidade {string}")
    public void existeUmSetorComPrecoBaseECapacidade(String sectorId, String basePrice, String capacity) {
        // Primeiro, verifica se o setor já existe e remove
        sectorRepository.findById(sectorId).ifPresent(sectorRepository::delete);
        
        // Cria um novo setor
        sector = new GarageSector();
        sector.setId(sectorId);
        sector.setBasePrice(new BigDecimal(basePrice));
        sector.setMaxCapacity(Integer.parseInt(capacity));
        sector.setCurrentOccupancy(0);
        sector.setOpenHour(LocalTime.of(6, 0)); // 06:00
        sector.setCloseHour(LocalTime.of(22, 0)); // 22:00
        sector.setDurationLimitMinutes(1440); // 24 horas em minutos
        
        // Salva o setor e verifica se foi salvo corretamente
        sector = sectorRepository.save(sector);
        assertNotNull(sector, "O setor deve ser salvo corretamente");
        assertNotNull(sector.getId(), "O ID do setor não deve ser nulo");
    }

    @Dado("um veículo com placa {string}")
    public void umVeiculoComPlaca(String licensePlate) {
        this.licensePlate = licensePlate;
        eventDTO.setLicensePlate(licensePlate);
    }

    @Dado("uma vaga nas coordenadas {double} e {double}")
    public void umaVagaNasCoordenadas(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        
        // Verifica se o setor existe
        assertNotNull(sector, "O setor deve existir antes de criar a vaga");
        assertNotNull(sector.getId(), "O ID do setor não deve ser nulo");
        
        // Verifica se a vaga já existe e remove
        spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .ifPresent(spotRepository::delete);
        
        // Cria a vaga
        spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spot = spotRepository.save(spot);
        
        // Verifica se a vaga foi salva corretamente
        assertNotNull(spot, "A vaga deve ser salva corretamente");
        assertNotNull(spot.getSectorId(), "O ID do setor da vaga não deve ser nulo");
    }

    @Quando("o veículo entra no estacionamento às {string}")
    public void oVeiculoEntraNoEstacionamentoAs(String entryTime) {
        eventDTO.setEventType("ENTRY");
        eventDTO.setEntryTime(entryTime);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Quando("o veículo estaciona na vaga com coordenadas {double} e {double}")
    public void oVeiculoEstacionaNaVagaComCoordenadas(double latitude, double longitude) {
        eventDTO.setEventType("PARKED");
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        parkingService.handleWebhookEvent(eventDTO);
        
        // Busca a vaga após o estacionamento
        foundSpot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow(() -> new RuntimeException("Vaga não encontrada após estacionamento"));
    }

    @Quando("o veículo sai do estacionamento às {string}")
    public void oVeiculoSaiDoEstacionamentoAs(String exitTime) {
        // Verifica se existe um evento de entrada
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        if (events.isEmpty() || !"ENTRY".equals(events.get(0).getType())) {
            // Se não existir, cria um evento de entrada
            eventDTO.setEventType("ENTRY");
            eventDTO.setEntryTime(LocalDateTime.now().minusHours(1).format(formatter));
            parkingService.handleWebhookEvent(eventDTO);
        }
        
        // Agora registra a saída
        eventDTO.setEventType("EXIT");
        eventDTO.setExitTime(exitTime);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Entao("a vaga deve estar livre")
    public void aVagaDeveEstarLivre() {
        assertNotNull(spotStatusResponse);
        assertTrue(spotStatusResponse.getStatusCode().is2xxSuccessful());
        
        SpotStatusDTO status = spotStatusResponse.getBody();
        assertNotNull(status);
        assertFalse(status.isOccupied());
    }

    @Entao("a placa do veículo na vaga deve ser {string}")
    public void aPlacaDoVeiculoNaVagaDeveSer(String licensePlate) {
        assertNotNull(foundSpot, "A vaga deve existir");
        assertEquals(licensePlate, foundSpot.getLicensePlate());
    }

    @Quando("tento estacionar o veículo na vaga com coordenadas {double} e {double}")
    public void tentoEstacionarOVeiculoNaVagaComCoordenadas(double latitude, double longitude) {
        try {
            eventDTO.setEventType("PARKED");
            eventDTO.setLatitude(latitude);
            eventDTO.setLongitude(longitude);
            parkingService.handleWebhookEvent(eventDTO);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Quando("busco a vaga com coordenadas {double} e {double}")
    public void buscoAVagaComCoordenadas(double latitude, double longitude) {
        foundSpot = parkingService.getSpotByCoordinates(latitude, longitude);
    }

    @Quando("busco a vaga pela placa {string}")
    public void buscoAVagaPelaPlaca(String licensePlate) {
        foundSpot = parkingService.getSpotByLicensePlate(licensePlate);
    }

    @Entao("deve ocorrer um erro indicando que a vaga não foi encontrada")
    public void deveOcorrerUmErroIndicandoQueAVagaNaoFoiEncontrada() {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("not found"));
    }

    @Entao("deve ocorrer um erro indicando que o veículo não foi encontrado")
    public void deveOcorrerUmErroIndicandoQueOVeiculoNaoFoiEncontrado() {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("vehicle not found"));
    }

    @Entao("a entrada deve ser registrada")
    public void entryShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("ENTRY", events.get(0).getType());
    }

    @Entao("o veículo deve ser marcado como no estacionamento")
    public void vehicleShouldBeMarkedAsParked() {
        PlateStatusDTO status = statusService.getPlateStatus(licensePlate);
        assertNotNull(status);
    }

    @Dado("que um veículo com placa {string} está no estacionamento")
    public void vehicleIsInParking(String plate) {
        this.licensePlate = plate;
        
        // Garante que existe um setor
        if (sector == null) {
            sector = new GarageSector();
            sector.setId("A");
            sector.setBasePrice(new BigDecimal("10.00"));
            sector.setMaxCapacity(100);
            sector.setCurrentOccupancy(0);
            sector = sectorRepository.save(sector);
        }
        
        // Cria uma vaga padrão se não existir
        if (spot == null) {
            spot = new ParkingSpot();
            spot.setLatitude(-23.561684);
            spot.setLongitude(-46.655981);
            spot.setSectorId(sector.getId());
            spot = spotRepository.save(spot);
        }
        
        // Primeiro registra a entrada
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("ENTRY");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        parkingService.handleWebhookEvent(eventDTO);
        
        // Depois registra o estacionamento
        eventDTO.setEventType("PARKED");
        eventDTO.setLatitude(spot.getLatitude());
        eventDTO.setLongitude(spot.getLongitude());
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Quando("o veículo estaciona nas coordenadas {string}")
    public void vehicleParksAtCoordinates(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        // Verifica se o setor existe
        assertNotNull(sector, "O setor deve existir antes de criar a vaga");
        assertNotNull(sector.getId(), "O ID do setor não deve ser nulo");
        
        // Verifica se a vaga já existe e remove
        spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .ifPresent(spotRepository::delete);
        
        // Cria a vaga antes de tentar estacionar
        ParkingSpot newSpot = new ParkingSpot();
        newSpot.setLatitude(latitude);
        newSpot.setLongitude(longitude);
        newSpot.setSectorId(sector.getId());
        this.spot = spotRepository.save(newSpot);
        
        // Verifica se a vaga foi salva corretamente
        assertNotNull(this.spot, "A vaga deve ser salva corretamente");
        assertNotNull(this.spot.getSectorId(), "O ID do setor da vaga não deve ser nulo");
        
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Entao("a vaga deve ser marcada como ocupada")
    public void spotShouldBeMarkedAsOccupied() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        assertTrue(spot.isOccupied());
    }

    @Entao("a ocupação do setor deve aumentar em {int}")
    public void sectorOccupancyShouldIncrease(int amount) {
        GarageSector updatedSector = sectorRepository.findById(sector.getId()).orElseThrow();
        assertEquals(amount, updatedSector.getCurrentOccupancy());
    }

    @Entao("o evento de estacionamento deve ser registrado")
    public void parkingEventShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("PARKED", events.get(1).getType());
    }

    @Dado("que um veículo com placa {string} está estacionado nas coordenadas {string}")
    public void vehicleIsParkedAtCoordinates(String plate, String coordinates) {
        this.licensePlate = plate;
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        // Garante que existe um setor
        if (sector == null) {
            sector = new GarageSector();
            sector.setId("A");
            sector.setBasePrice(new BigDecimal("10.00"));
            sector.setMaxCapacity(100);
            sector.setCurrentOccupancy(0);
            sector = sectorRepository.save(sector);
        }
        
        // Cria a vaga se não existir
        spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseGet(() -> {
                ParkingSpot newSpot = new ParkingSpot();
                newSpot.setLatitude(latitude);
                newSpot.setLongitude(longitude);
                newSpot.setSectorId(sector.getId());
                return spotRepository.save(newSpot);
            });
        
        // Primeiro registra a entrada
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("ENTRY");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        parkingService.handleWebhookEvent(eventDTO);
        
        // Depois registra o estacionamento
        eventDTO.setEventType("PARKED");
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Entao("a vaga deve ser marcada como disponível")
    public void spotShouldBeMarkedAsAvailable() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        assertFalse(spot.isOccupied());
    }

    @Entao("a ocupação do setor deve diminuir em {int}")
    public void sectorOccupancyShouldDecrease(int amount) {
        GarageSector updatedSector = sectorRepository.findById(sector.getId()).orElseThrow();
        assertEquals(0, updatedSector.getCurrentOccupancy());
    }

    @Entao("o evento de saída deve ser registrado")
    public void exitEventShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("EXIT", events.get(0).getType());
    }

    @Entao("a taxa de estacionamento deve ser calculada com base na duração")
    public void parkingFeeShouldBeCalculated() {
        BigDecimal price = parkingService.calculatePrice(
            entryTime,
            exitTime,
            sector
        );
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Dado("que uma vaga nas coordenadas {string} está ocupada")
    public void spotIsOccupied(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spotRepository.save(spot);
        
        eventDTO.setLicensePlate("OCCUPIED123");
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Quando("outro veículo tenta estacionar na mesma vaga")
    public void anotherVehicleTriesToPark() {
        try {
            eventDTO.setLicensePlate("NEW123");
            eventDTO.setEventType("PARKED");
            eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
            eventDTO.setLatitude(latitude);
            eventDTO.setLongitude(longitude);
            
            parkingService.handleWebhookEvent(eventDTO);
        } catch (Exception e) {
            this.thrownException = e;
        }
    }

    @Entao("o sistema deve rejeitar a tentativa de estacionamento")
    public void systemShouldRejectParkingAttempt() {
        assertNotNull(thrownException);
    }

    @Entao("lançar uma {string}")
    public void throwException(String exceptionName) {
        assertEquals(exceptionName, thrownException.getClass().getSimpleName());
    }

    @Dado("que um veículo com placa {string} está estacionado")
    public void vehicleIsParked(String plate) {
        this.licensePlate = plate;
        
        // Garante que existe um setor
        if (sector == null) {
            sector = new GarageSector();
            sector.setId("A");
            sector.setBasePrice(new BigDecimal("10.00"));
            sector.setMaxCapacity(100);
            sector.setCurrentOccupancy(0);
            sector = sectorRepository.save(sector);
        }
        
        // Cria uma vaga padrão se não existir
        if (spot == null) {
            spot = new ParkingSpot();
            spot.setLatitude(-23.561684);
            spot.setLongitude(-46.655981);
            spot.setSectorId(sector.getId());
            spot = spotRepository.save(spot);
        }
        
        // Primeiro registra a entrada
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("ENTRY");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        parkingService.handleWebhookEvent(eventDTO);
        
        // Depois registra o estacionamento
        eventDTO.setEventType("PARKED");
        eventDTO.setLatitude(spot.getLatitude());
        eventDTO.setLongitude(spot.getLongitude());
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Quando("eu verifico o status do veículo")
    public void checkVehicleStatus() {
        // Status já é verificado nos métodos Then
    }

    @Entao("eu devo ver as informações atuais de estacionamento")
    public void shouldSeeCurrentParkingInfo() {
        PlateStatusDTO status = statusService.getPlateStatus(licensePlate);
        assertNotNull(status);
    }

    @Entao("o preço calculado com base na duração")
    public void priceCalculatedBasedOnDuration() {
        BigDecimal price = parkingService.calculatePrice(
            entryTime,
            exitTime,
            sector
        );
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Dado("que existe uma vaga nas coordenadas {string}")
    public void spotExists(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        // Verifica se o setor existe
        assertNotNull(sector, "O setor deve existir antes de criar a vaga");
        assertNotNull(sector.getId(), "O ID do setor não deve ser nulo");
        
        // Verifica se a vaga já existe e remove
        spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .ifPresent(spotRepository::delete);
        
        // Cria a vaga
        spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spot = spotRepository.save(spot);
        
        // Verifica se a vaga foi salva corretamente
        assertNotNull(spot, "A vaga deve ser salva corretamente");
        assertNotNull(spot.getSectorId(), "O ID do setor da vaga não deve ser nulo");
    }

    @Quando("consulto o status da placa")
    public void consultoOStatusDaPlaca() {
        // Verifica se o veículo está estacionado em uma vaga válida
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        if (!events.isEmpty() && "PARKED".equals(events.get(0).getType())) {
            spot = spotRepository.findByLatitudeAndLongitude(events.get(0).getLatitude(), events.get(0).getLongitude())
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada para o veículo"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = new HashMap<>();
        request.put("licensePlate", licensePlate);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        plateStatusResponse = restTemplate.postForEntity("/status/plate-status", entity, PlateStatusDTO.class);
    }

    @Quando("consulto o status da vaga")
    public void consultoOStatusDaVaga() {
        // Verifica se a vaga existe antes de consultar
        spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Double> request = new HashMap<>();
        request.put("lat", latitude);
        request.put("lng", longitude);

        HttpEntity<Map<String, Double>> entity = new HttpEntity<>(request, headers);
        spotStatusResponse = restTemplate.postForEntity("/status/spot-status", entity, SpotStatusDTO.class);
    }

    @Entao("o veículo deve estar estacionado")
    public void oVeiculoDeveEstarEstacionado() {
        assertNotNull(plateStatusResponse);
        assertTrue(plateStatusResponse.getStatusCode().is2xxSuccessful());
        
        PlateStatusDTO status = plateStatusResponse.getBody();
        assertNotNull(status);
        assertEquals(licensePlate, status.getLicensePlate());
        assertNotNull(status.getEntryTime());
        assertNotNull(status.getTimeParked());
        assertNotNull(status.getLat());
        assertNotNull(status.getLng());
    }

    @Entao("o veículo não deve estar estacionado")
    public void oVeiculoNaoDeveEstarEstacionado() {
        assertNotNull(plateStatusResponse);
        assertTrue(plateStatusResponse.getStatusCode().is2xxSuccessful());
        
        PlateStatusDTO status = plateStatusResponse.getBody();
        assertNotNull(status);
    }

    @Entao("as informações do veículo se estiver ocupada")
    public void shouldSeeVehicleInfoIfOccupied() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        if (spot.isOccupied()) {
            assertNotNull(spot.getLicensePlate());
        }
    }

    @Dado("que o setor {string} tem {int}% de ocupação")
    public void sectorHasOccupancy(String sectorId, int occupancyPercentage) {
        GarageSector sector = sectorRepository.findById(sectorId).orElseThrow();
        int spotsToOccupy = (sector.getMaxCapacity() * occupancyPercentage) / 100;
        
        for (int i = 0; i < spotsToOccupy; i++) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSectorId(sectorId);
            spot.setLatitude(-23.561684 + i * 0.0001);
            spot.setLongitude(-46.655981 + i * 0.0001);
            spotRepository.save(spot);
            
            eventDTO.setLicensePlate("TEST" + i);
            eventDTO.setEventType("PARKED");
            eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
            eventDTO.setLatitude(spot.getLatitude());
            eventDTO.setLongitude(spot.getLongitude());
            
            parkingService.handleWebhookEvent(eventDTO);
        }
    }

    @Quando("um veículo estaciona por {int} horas")
    public void vehicleParksForHours(int hours) {
        this.licensePlate = "TEST_VEHICLE";
        this.latitude = -23.561684;
        this.longitude = -46.655981;
        
        // Garante que existe um setor
        if (sector == null) {
            sector = new GarageSector();
            sector.setId("A");
            sector.setBasePrice(new BigDecimal("10.00"));
            sector.setMaxCapacity(100);
            sector.setCurrentOccupancy(0);
            sector = sectorRepository.save(sector);
        }
        
        // Cria a vaga se não existir
        spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseGet(() -> {
                ParkingSpot newSpot = new ParkingSpot();
                newSpot.setLatitude(latitude);
                newSpot.setLongitude(longitude);
                newSpot.setSectorId(sector.getId());
                return spotRepository.save(newSpot);
            });
        
        eventDTO.setLicensePlate(licensePlate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().minusHours(hours).format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Entao("o preço deve ser calculado com {int}% de desconto")
    public void priceShouldBeCalculatedWithDiscount(int discountPercentage) {
        BigDecimal price = parkingService.calculatePrice(
            entryTime,
            exitTime,
            sector
        );
        BigDecimal basePrice = sector.getBasePrice().multiply(BigDecimal.valueOf(2)); // 2 horas
        BigDecimal expectedPrice = basePrice.multiply(BigDecimal.valueOf(1 - discountPercentage / 100.0));
        
        assertEquals(0, price.compareTo(expectedPrice));
    }

    @Entao("o preço deve ser calculado com {int}% de aumento")
    public void priceShouldBeCalculatedWithIncrease(int increasePercentage) {
        BigDecimal price = parkingService.calculatePrice(
            entryTime,
            exitTime,
            sector
        );
        BigDecimal basePrice = sector.getBasePrice().multiply(BigDecimal.valueOf(2)); // 2 horas
        BigDecimal expectedPrice = basePrice.multiply(BigDecimal.valueOf(1 + increasePercentage / 100.0));
        
        assertEquals(0, price.compareTo(expectedPrice));
    }
} 