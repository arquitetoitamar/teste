package com.estapar.parking.controller;

import com.estapar.parking.config.SecurityTestConfig;
import com.estapar.parking.dto.VehicleEventDTO;
import com.estapar.parking.dto.WebhookResponse;
import com.estapar.parking.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@Import(SecurityTestConfig.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParkingService parkingService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Test
    void handleWebhook_WhenValidEntryEvent_ShouldReturnSuccess() throws Exception {
        // Arrange
        VehicleEventDTO event = new VehicleEventDTO();
        event.setLicensePlate("ABC1234");
        event.setEventType("ENTRY");
        event.setEntryTime(LocalDateTime.now().format(formatter));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event processed successfully"));

        verify(parkingService).handleWebhookEvent(any(VehicleEventDTO.class));
    }

    @Test
    void handleWebhook_WhenValidParkedEvent_ShouldReturnSuccess() throws Exception {
        // Arrange
        VehicleEventDTO event = new VehicleEventDTO();
        event.setLicensePlate("ABC1234");
        event.setEventType("PARKED");
        event.setLatitude(-23.550520);
        event.setLongitude(-46.633308);

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event processed successfully"));

        verify(parkingService).handleWebhookEvent(any(VehicleEventDTO.class));
    }

    @Test
    void handleWebhook_WhenValidExitEvent_ShouldReturnSuccess() throws Exception {
        // Arrange
        VehicleEventDTO event = new VehicleEventDTO();
        event.setLicensePlate("ABC1234");
        event.setEventType("EXIT");
        event.setExitTime(LocalDateTime.now().format(formatter));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event processed successfully"));

        verify(parkingService).handleWebhookEvent(any(VehicleEventDTO.class));
    }

    @Test
    void handleWebhook_WhenNullEvent_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Event cannot be null"));
    }

    @Test
    void handleWebhook_WhenEventTypeIsMissing_ShouldReturnBadRequest() throws Exception {
        // Arrange
        VehicleEventDTO event = new VehicleEventDTO();
        event.setLicensePlate("ABC1234");

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Event cannot be null"));
    }

    @Test
    void handleWebhook_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        VehicleEventDTO event = new VehicleEventDTO();
        event.setLicensePlate("ABC1234");
        event.setEventType("ENTRY");
        event.setEntryTime(LocalDateTime.now().format(formatter));

        doThrow(new RuntimeException("Test error"))
                .when(parkingService).handleWebhookEvent(any(VehicleEventDTO.class));

        // Act & Assert
        mockMvc.perform(post("/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error processing event: Test error"));
    }
} 