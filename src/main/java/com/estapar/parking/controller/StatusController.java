package com.estapar.parking.controller;

import com.estapar.parking.dto.PlateStatusDTO;
import com.estapar.parking.dto.PlateStatusRequest;
import com.estapar.parking.dto.SpotStatusDTO;
import com.estapar.parking.dto.SpotStatusRequest;
import com.estapar.parking.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Status", description = "APIs para consulta de status")
public class StatusController {
    private final StatusService statusService;

    @PostMapping("/plate-status")
    @Operation(summary = "Consulta status de uma placa")
    public ResponseEntity<PlateStatusDTO> getPlateStatus(@RequestBody PlateStatusRequest request) {
        return ResponseEntity.ok(statusService.getPlateStatus(request.getLicensePlate()));
    }

    @PostMapping("/spot-status")
    @Operation(summary = "Consulta status de uma vaga")
    public ResponseEntity<SpotStatusDTO> getSpotStatus(@RequestBody SpotStatusRequest request) {
        return ResponseEntity.ok(statusService.getSpotStatus(request.getLat(), request.getLng()));
    }
} 