package com.emersondev.api.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleVentaResponse {
  private Long id;
  private ProductoInfo producto;
  private ColorInfo color;
  private TallaInfo talla;
  private Integer cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal subtotal;
  private String descripcionProducto;

  @Data
  public static class ProductoInfo {
    private Long id;
    private String codigo;
    private String nombre;
    private String marca;
  }

  @Data
  public static class ColorInfo {
    private Long id;
    private String nombre;
  }

  @Data
  public static class TallaInfo {
    private Long id;
    private String numero;
  }
}
