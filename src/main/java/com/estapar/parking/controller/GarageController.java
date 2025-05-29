package com.estapar.parking.controller;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Garage", description = "APIs para configuração da garagem")
public class GarageController {
    private final GarageService garageService;

    @GetMapping("/garage")
    @Operation(summary = "Obtém configuração atual da garagem")
    public ResponseEntity<GarageConfigDTO> getCurrentConfig() {
        return ResponseEntity.ok(garageService.getCurrentConfig());
    }

    @PostMapping("/garage/import")
    @Operation(summary = "Importa nova configuração da garagem")
    public ResponseEntity<Void> importGarageConfig(@RequestBody GarageConfigDTO config) {
        garageService.importGarageConfig(config);
        return ResponseEntity.ok().build();
    }
} 