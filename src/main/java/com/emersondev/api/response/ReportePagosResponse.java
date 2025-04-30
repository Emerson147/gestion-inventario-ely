package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePagosResponse {
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaFin;
  private BigDecimal montoTotal;
  private Integer cantidadPagos;
  private List<Map<String, Object>> estadisticasPorMetodo = new ArrayList<>();
  private List<Map<String, Object>> estadisticasPorFecha = new ArrayList<>();
  private List<Map<String, Object>> distribucionPorcentual = new ArrayList<>();
}
