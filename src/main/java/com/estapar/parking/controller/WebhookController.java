package com.estapar.parking.controller;

import com.estapar.parking.dto.VehicleEventDTO;
import com.estapar.parking.dto.WebhookResponse;
import com.estapar.parking.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final ParkingService parkingService;

    @PostMapping
    public ResponseEntity<WebhookResponse> handleWebhook(@RequestBody VehicleEventDTO event) {
        if (event == null) {
            return ResponseEntity.badRequest().body(new WebhookResponse(false, "Event cannot be null"));
        }

        try {
            parkingService.handleWebhookEvent(event);
            return ResponseEntity.ok(new WebhookResponse(true, "Event processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new WebhookResponse(false, "Error processing event: " + e.getMessage()));
        }
    }
} 