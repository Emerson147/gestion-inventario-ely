package com.emersondev.api.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentasMetricasResponse {
  private String periodo;
  private BigDecimal totalVentas;
  private int cantidadVentas;
  private int cantidadProductos;
  private BigDecimal ticketPromedio;
  private BigDecimal comparativaVentas; // diferencia con periodo anterior
  private double porcentajeCrecimiento;
  private String tendencia; // positiva o negativa
  private List<TopProducto> topProductos;
  private List<TopVendedor> topVendedores;
  private List<Map<String, Object>> ventasPorCategoria;
  private GraficoVentas graficoVentas;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TopProducto {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private int cantidadVendida;
    private BigDecimal totalVendido;
    private BigDecimal precioPromedio;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TopVendedor {
    private Long id;
    private String nombre;
    private int cantidadVentas;
    private BigDecimal totalVentas;
    private BigDecimal ticketPromedio;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GraficoVentas {
    private List<String> etiquetas;
    private List<BigDecimal> valores;
  }
}

