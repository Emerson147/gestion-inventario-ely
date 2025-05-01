package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteComprobantesResponse {
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private BigDecimal totalFacturado;
  private BigDecimal totalIGV;
  private Integer cantidadComprobantes;
  private List<Map<String, Object>> resumenPorTipo = new ArrayList<>();
  private List<Map<String, Object>> topClientes = new ArrayList<>();
}
