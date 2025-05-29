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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    }

    @Given("que o sistema de estacionamento está operacional")
    public void systemIsOperational() {
        // Sistema está pronto para testes
    }

    @Given("existe um setor {string} com preço base {string} e capacidade {int}")
    public void createSector(String sectorId, String basePrice, int capacity) {
        sector = new GarageSector();
        sector.setId(sectorId);
        sector.setBasePrice(new BigDecimal(basePrice));
        sector.setMaxCapacity(capacity);
        sector.setCurrentOccupancy(0);
        sectorRepository.save(sector);
    }

    @Given("um veículo com placa {string}")
    public void umVeiculoComPlaca(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Given("uma vaga nas coordenadas {double} e {double}")
    public void umaVagaNasCoordenadas(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @When("o veículo entra no estacionamento às {string}")
    public void oVeiculoEntraNoEstacionamentoAs(String entryTime) {
        eventDTO.setEventType("ENTRY");
        eventDTO.setEntryTime(entryTime);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @When("o veículo estaciona na vaga com coordenadas {double} e {double}")
    public void oVeiculoEstacionaNaVagaComCoordenadas(double latitude, double longitude) {
        eventDTO.setEventType("PARKED");
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @When("o veículo sai do estacionamento às {string}")
    public void oVeiculoSaiDoEstacionamentoAs(String exitTime) {
        eventDTO.setEventType("EXIT");
        eventDTO.setExitTime(exitTime);
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Then("a vaga deve estar ocupada")
    public void aVagaDeveEstarOcupada() {
        assertNotNull(spotStatusResponse);
        assertTrue(spotStatusResponse.getStatusCode().is2xxSuccessful());
        
        SpotStatusDTO status = spotStatusResponse.getBody();
        assertNotNull(status);
        assertTrue(status.isOccupied());
        assertNotNull(status.getLicensePlate());
        assertNotNull(status.getEntryTime());
        assertNotNull(status.getTimeParked());
    }

    @Then("a vaga deve estar livre")
    public void aVagaDeveEstarLivre() {
        assertNotNull(spotStatusResponse);
        assertTrue(spotStatusResponse.getStatusCode().is2xxSuccessful());
        
        SpotStatusDTO status = spotStatusResponse.getBody();
        assertNotNull(status);
        assertFalse(status.isOccupied());
        assertEquals("", status.getLicensePlate());
        assertEquals(BigDecimal.ZERO, status.getPriceUntilNow());
        assertNull(status.getEntryTime());
        assertNotNull(status.getTimeParked());
    }

    @Then("a placa do veículo na vaga deve ser {string}")
    public void aPlacaDoVeiculoNaVagaDeveSer(String licensePlate) {
        assertEquals(licensePlate, foundSpot.getLicensePlate());
    }

    @When("tento estacionar o veículo na vaga com coordenadas {double} e {double}")
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

    @Then("deve ocorrer um erro indicando que a vaga está ocupada")
    public void deveOcorrerUmErroIndicandoQueAVagaEstaOcupada() {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("occupied"));
    }

    @When("busco a vaga com coordenadas {double} e {double}")
    public void buscoAVagaComCoordenadas(double latitude, double longitude) {
        foundSpot = parkingService.getSpotByCoordinates(latitude, longitude);
    }

    @When("busco a vaga pela placa {string}")
    public void buscoAVagaPelaPlaca(String licensePlate) {
        foundSpot = parkingService.getSpotByLicensePlate(licensePlate);
    }

    @Then("deve ocorrer um erro indicando que a vaga não foi encontrada")
    public void deveOcorrerUmErroIndicandoQueAVagaNaoFoiEncontrada() {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("not found"));
    }

    @Then("deve ocorrer um erro indicando que o veículo não foi encontrado")
    public void deveOcorrerUmErroIndicandoQueOVeiculoNaoFoiEncontrado() {
        assertNotNull(thrownException);
        assertTrue(thrownException.getMessage().contains("vehicle not found"));
    }

    @Then("a entrada deve ser registrada")
    public void entryShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("PARKED", events.get(0).getType());
    }

    @Then("o veículo deve ser marcado como no estacionamento")
    public void vehicleShouldBeMarkedAsParked() {
        PlateStatusDTO status = statusService.getPlateStatus(licensePlate);
        assertNotNull(status.getEntryTime());
        assertNotNull(status.getLat());
        assertNotNull(status.getLng());
    }

    @Given("que um veículo com placa {string} está no estacionamento")
    public void vehicleIsInParking(String plate) {
        this.licensePlate = plate;
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(0.0);
        eventDTO.setLongitude(0.0);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @When("o veículo estaciona nas coordenadas {string}")
    public void vehicleParksAtCoordinates(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate(licensePlate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Then("a vaga deve ser marcada como ocupada")
    public void spotShouldBeMarkedAsOccupied() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        assertTrue(spot.isOccupied());
    }

    @Then("a ocupação do setor deve aumentar em {int}")
    public void sectorOccupancyShouldIncrease(int amount) {
        GarageSector updatedSector = sectorRepository.findById(sector.getId()).orElseThrow();
        assertEquals(amount, updatedSector.getCurrentOccupancy());
    }

    @Then("o evento de estacionamento deve ser registrado")
    public void parkingEventShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("PARKED", events.get(0).getType());
    }

    @Given("que um veículo com placa {string} está estacionado nas coordenadas {string}")
    public void vehicleIsParkedAtCoordinates(String plate, String coordinates) {
        this.licensePlate = plate;
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Then("a vaga deve ser marcada como disponível")
    public void spotShouldBeMarkedAsAvailable() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        assertFalse(spot.isOccupied());
    }

    @Then("a ocupação do setor deve diminuir em {int}")
    public void sectorOccupancyShouldDecrease(int amount) {
        GarageSector updatedSector = sectorRepository.findById(sector.getId()).orElseThrow();
        assertEquals(0, updatedSector.getCurrentOccupancy());
    }

    @Then("o evento de saída deve ser registrado")
    public void exitEventShouldBeRecorded() {
        List<ParkingEvent> events = eventRepository.findByLicensePlateOrderByTimestampDesc(licensePlate);
        assertFalse(events.isEmpty());
        assertEquals("EXIT", events.get(0).getType());
    }

    @Then("a taxa de estacionamento deve ser calculada com base na duração")
    public void parkingFeeShouldBeCalculated() {
        BigDecimal price = parkingService.calculatePrice(licensePlate, LocalDateTime.now());
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Given("que uma vaga nas coordenadas {string} está ocupada")
    public void spotIsOccupied(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spotRepository.save(spot);
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate("OCCUPIED123");
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @When("outro veículo tenta estacionar na mesma vaga")
    public void anotherVehicleTriesToPark() {
        try {
            eventDTO = new VehicleEventDTO();
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

    @Then("o sistema deve rejeitar a tentativa de estacionamento")
    public void systemShouldRejectParkingAttempt() {
        assertNotNull(thrownException);
    }

    @Then("lançar uma {string}")
    public void throwException(String exceptionName) {
        assertEquals(exceptionName, thrownException.getClass().getSimpleName());
    }

    @Given("que um veículo com placa {string} está estacionado")
    public void vehicleIsParked(String plate) {
        this.licensePlate = plate;
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate(plate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @When("eu verifico o status do veículo")
    public void checkVehicleStatus() {
        // Status já é verificado nos métodos Then
    }

    @Then("eu devo ver as informações atuais de estacionamento")
    public void shouldSeeCurrentParkingInfo() {
        PlateStatusDTO status = statusService.getPlateStatus(licensePlate);
        assertNotNull(status.getEntryTime());
        assertNotNull(status.getLat());
        assertNotNull(status.getLng());
    }

    @Then("o preço calculado com base na duração")
    public void priceCalculatedBasedOnDuration() {
        BigDecimal price = parkingService.calculatePrice(licensePlate, LocalDateTime.now());
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Given("que existe uma vaga nas coordenadas {string}")
    public void spotExists(String coordinates) {
        String[] coords = coordinates.split(", ");
        this.latitude = Double.parseDouble(coords[0]);
        this.longitude = Double.parseDouble(coords[1]);
        
        spot = new ParkingSpot();
        spot.setLatitude(latitude);
        spot.setLongitude(longitude);
        spot.setSectorId(sector.getId());
        spotRepository.save(spot);
    }

    @When("consulto o status da placa")
    public void consultoOStatusDaPlaca() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = new HashMap<>();
        request.put("licensePlate", licensePlate);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        plateStatusResponse = restTemplate.postForEntity("/status/plate-status", entity, PlateStatusDTO.class);
    }

    @When("consulto o status da vaga")
    public void consultoOStatusDaVaga() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Double> request = new HashMap<>();
        request.put("lat", latitude);
        request.put("lng", longitude);

        HttpEntity<Map<String, Double>> entity = new HttpEntity<>(request, headers);
        spotStatusResponse = restTemplate.postForEntity("/status/spot-status", entity, SpotStatusDTO.class);
    }

    @Then("o veículo deve estar estacionado")
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

    @Then("o veículo não deve estar estacionado")
    public void oVeiculoNaoDeveEstarEstacionado() {
        assertNotNull(plateStatusResponse);
        assertTrue(plateStatusResponse.getStatusCode().is2xxSuccessful());
        
        PlateStatusDTO status = plateStatusResponse.getBody();
        assertNotNull(status);
        assertEquals(licensePlate, status.getLicensePlate());
        assertEquals(BigDecimal.ZERO, status.getPriceUntilNow());
        assertNull(status.getEntryTime());
        assertNotNull(status.getTimeParked());
        assertNull(status.getLat());
        assertNull(status.getLng());
    }

    @Then("as informações do veículo se estiver ocupada")
    public void shouldSeeVehicleInfoIfOccupied() {
        ParkingSpot spot = spotRepository.findByLatitudeAndLongitude(latitude, longitude)
            .orElseThrow();
        if (spot.isOccupied()) {
            assertNotNull(spot.getLicensePlate());
        }
    }

    @Given("que o setor {string} tem {int}% de ocupação")
    public void sectorHasOccupancy(String sectorId, int occupancyPercentage) {
        GarageSector sector = sectorRepository.findById(sectorId).orElseThrow();
        int spotsToOccupy = (sector.getMaxCapacity() * occupancyPercentage) / 100;
        
        for (int i = 0; i < spotsToOccupy; i++) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSectorId(sectorId);
            spot.setLatitude(-23.561684 + i * 0.0001);
            spot.setLongitude(-46.655981 + i * 0.0001);
            spotRepository.save(spot);
            
            eventDTO = new VehicleEventDTO();
            eventDTO.setLicensePlate("TEST" + i);
            eventDTO.setEventType("PARKED");
            eventDTO.setEntryTime(LocalDateTime.now().format(formatter));
            eventDTO.setLatitude(spot.getLatitude());
            eventDTO.setLongitude(spot.getLongitude());
            
            parkingService.handleWebhookEvent(eventDTO);
        }
    }

    @When("um veículo estaciona por {int} horas")
    public void vehicleParksForHours(int hours) {
        this.licensePlate = "TEST_VEHICLE";
        this.latitude = -23.561684;
        this.longitude = -46.655981;
        
        eventDTO = new VehicleEventDTO();
        eventDTO.setLicensePlate(licensePlate);
        eventDTO.setEventType("PARKED");
        eventDTO.setEntryTime(LocalDateTime.now().minusHours(hours).format(formatter));
        eventDTO.setLatitude(latitude);
        eventDTO.setLongitude(longitude);
        
        parkingService.handleWebhookEvent(eventDTO);
    }

    @Then("o preço deve ser calculado com {int}% de desconto")
    public void priceShouldBeCalculatedWithDiscount(int discountPercentage) {
        BigDecimal price = parkingService.calculatePrice(licensePlate, LocalDateTime.now());
        BigDecimal basePrice = sector.getBasePrice().multiply(BigDecimal.valueOf(2)); // 2 horas
        BigDecimal expectedPrice = basePrice.multiply(BigDecimal.valueOf(1 - discountPercentage / 100.0));
        
        assertEquals(0, price.compareTo(expectedPrice));
    }

    @Then("o preço deve ser calculado com {int}% de aumento")
    public void priceShouldBeCalculatedWithIncrease(int increasePercentage) {
        BigDecimal price = parkingService.calculatePrice(licensePlate, LocalDateTime.now());
        BigDecimal basePrice = sector.getBasePrice().multiply(BigDecimal.valueOf(2)); // 2 horas
        BigDecimal expectedPrice = basePrice.multiply(BigDecimal.valueOf(1 + increasePercentage / 100.0));
        
        assertEquals(0, price.compareTo(expectedPrice));
    }
} 