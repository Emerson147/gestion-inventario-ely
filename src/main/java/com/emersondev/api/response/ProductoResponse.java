package com.emersondev.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductoResponse {
  private Long id;
  private String codigo;
  private String nombre;
  private String descripcion;
  private String marca;
  private String modelo;
  private BigDecimal precioCompra;
  private BigDecimal precioVenta;
  private String imagen;
  private List<ColorResponse> colores;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
