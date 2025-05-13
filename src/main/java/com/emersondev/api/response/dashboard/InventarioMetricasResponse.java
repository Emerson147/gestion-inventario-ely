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
public class InventarioMetricasResponse {
  private int totalProductos;
  private int productosSinStock;
  private int productosStockBajo;
  private int productosSinMovimiento;
  private BigDecimal valorInventarioTotal;
  private List<ProductoSinMovimiento> productosSinMovimientoDetalle;
  private List<ProductoStockBajo> productosStockBajoDetalle;
  private List<Map<String, Object>> distribucionPorCategoria;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductoSinMovimiento {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private int stock;
    private String ultimaVenta;
    private int diasSinMovimiento;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductoStockBajo {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private int cantidadFaltante;
  }
}

