package com.estapar.parking.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RevenueDTO {
    private BigDecimal amount;
    private String currency;
    private String sectorId;
} 