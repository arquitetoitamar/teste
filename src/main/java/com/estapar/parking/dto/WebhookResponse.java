package com.estapar.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebhookResponse {
    private boolean success;
    private String message;
} 