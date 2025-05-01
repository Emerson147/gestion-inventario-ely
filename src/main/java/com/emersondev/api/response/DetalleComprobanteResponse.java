package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleComprobanteResponse {
  private Long id;
  private ProductoInfo producto;
  private ColorInfo color;
  private TallaInfo talla;
  private Integer cantidad;
  private BigDecimal precioUnitario;
  private BigDecimal subtotal;
  private BigDecimal igv;
  private BigDecimal total;
  private String descripcion;
  private String unidadMedida;
  private String codigoProducto;

  @Data
  public static class ProductoInfo {
    private Long id;
    private String codigo;
    private String nombre;
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
