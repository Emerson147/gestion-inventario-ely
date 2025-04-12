package com.emersondev.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ResumeVentaResponse {
  private Long totalVentas;
  private BigDecimal montoTotal;
  private BigDecimal promedioPorVenta;
  private Long totalProductosVendidos;
  private Map<String, Long> ventasPorProducto;
  private Map<String, BigDecimal> ventasPorDia;
  private Map<String, Long> ventasPorEstado;
}
