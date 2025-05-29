package com.estapar.parking.controller;

import com.estapar.parking.dto.VehicleEventDTO;
import com.estapar.parking.dto.WebhookResponse;
import com.estapar.parking.service.ParkingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private WebhookController controller;

    private VehicleEventDTO entryEvent;
    private VehicleEventDTO parkedEvent;
    private VehicleEventDTO exitEvent;

    @BeforeEach
    void setUp() {
        // Entry event
        entryEvent = new VehicleEventDTO();
        entryEvent.setLicensePlate("ZUL0001");
        entryEvent.setEntryTime("2025-01-01T12:00:00.000Z");
        entryEvent.setEventType("ENTRY");

        // Parked event
        parkedEvent = new VehicleEventDTO();
        parkedEvent.setLicensePlate("ZUL0001");
        parkedEvent.setLatitude(-23.561684);
        parkedEvent.setLongitude(-46.655981);
        parkedEvent.setEventType("PARKED");

        // Exit event
        exitEvent = new VehicleEventDTO();
        exitEvent.setLicensePlate("ZUL0001");
        exitEvent.setExitTime("2025-01-01T12:00:00.000Z");
        exitEvent.setEventType("EXIT");
    }

    @Test
    void handleWebhook_WhenEntryEventIsValid_ShouldReturnOk() {
        // Test
        ResponseEntity<WebhookResponse> response = controller.handleWebhook(entryEvent);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Event processed successfully", response.getBody().getMessage());
        verify(parkingService, times(1)).handleWebhookEvent(entryEvent);
    }

    @Test
    void handleWebhook_WhenParkedEventIsValid_ShouldReturnOk() {
        // Test
        ResponseEntity<WebhookResponse> response = controller.handleWebhook(parkedEvent);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Event processed successfully", response.getBody().getMessage());
        verify(parkingService, times(1)).handleWebhookEvent(parkedEvent);
    }

    @Test
    void handleWebhook_WhenExitEventIsValid_ShouldReturnOk() {
        // Test
        ResponseEntity<WebhookResponse> response = controller.handleWebhook(exitEvent);

        // Verify
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Event processed successfully", response.getBody().getMessage());
        verify(parkingService, times(1)).handleWebhookEvent(exitEvent);
    }

    @Test
    void handleWebhook_WhenDTOIsNull_ShouldReturnBadRequest() {
        // Test
        ResponseEntity<WebhookResponse> response = controller.handleWebhook(null);

        // Verify
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Event cannot be null", response.getBody().getMessage());
        verify(parkingService, never()).handleWebhookEvent(any());
    }

    @Test
    void handleWebhook_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Setup
        String errorMessage = "Test error";
        doThrow(new RuntimeException(errorMessage)).when(parkingService).handleWebhookEvent(entryEvent);

        // Test
        ResponseEntity<WebhookResponse> response = controller.handleWebhook(entryEvent);

        // Verify
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Error processing event: " + errorMessage, response.getBody().getMessage());
        verify(parkingService, times(1)).handleWebhookEvent(entryEvent);
    }
} 