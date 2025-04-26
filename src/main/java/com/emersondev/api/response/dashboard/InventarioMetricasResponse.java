package com.emersondev.api.response.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class InventarioMetricasResponse {
  private List<ProductoStock> productosStockBajo;
  private Map<String, BigDecimal> valorPorAlmacen;
  private List<ProductoStock> productosSinMovimiento;
}

@Data
class ProductoStock {
  private Long id;
  private String nombre;
  private Integer cantidad;
  private String almacen;
}
