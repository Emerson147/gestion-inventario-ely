package com.emersondev.api.response.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class VentasMetricasResponse {
  private String periodo;
  private Map<String, BigDecimal> ventasPorDia;
  private List<ProductoVendido> productosMasVendidos;
  private Map<String, BigDecimal> ventasPorMetodoPago;

}

@Data
class ProductoVendido {
  private Long id;
  private String nombre;
  private Integer cantidad;
  private BigDecimal total;
}


