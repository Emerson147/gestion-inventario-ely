package com.emersondev.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaResponse {
  private Long id;
  private InventarioResponse inventario;
  private Integer cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal subtotal;
}
