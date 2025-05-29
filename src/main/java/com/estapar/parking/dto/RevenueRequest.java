package com.estapar.parking.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class RevenueRequest implements Serializable {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String date;
    private String sector;
} 