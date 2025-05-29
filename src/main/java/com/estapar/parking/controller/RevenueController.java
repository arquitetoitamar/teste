package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueRequest;
import com.estapar.parking.dto.RevenueResponse;
import com.estapar.parking.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Revenue", description = "APIs para consulta de faturamento")
public class RevenueController {
    private final RevenueService revenueService;

    @PostMapping("/revenue")
    @Operation(summary = "Consulta faturamento por data e setor")
    public ResponseEntity<RevenueResponse> getRevenue(@RequestBody RevenueRequest request) {
        return ResponseEntity.ok(revenueService.getRevenue(request.getDate(), request.getSector()));
    }
} 