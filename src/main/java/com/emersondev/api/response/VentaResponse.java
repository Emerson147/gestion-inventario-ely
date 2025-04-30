package com.emersondev.api.response;

import com.emersondev.domain.entity.Venta;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class VentaResponse {

  private Long id;
  private String numeroVenta;
  private ClienteInfo cliente;
  private UsuarioInfo usuario;
  private BigDecimal subtotal;
  private BigDecimal igv;
  private BigDecimal total;
  private String estado;
  private String tipoComprobante;
  private String serieComprobante;
  private String numeroComprobante;
  private String observaciones;
  private List<DetalleVentaResponse> detalles = new ArrayList<>();
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  @Data
  public static class ClienteInfo {
    private Long id;
    private String nombres;
    private String apellidos;
    private String documento;  // Combinación de DNI o RUC según disponibilidad
  }

  @Data
  public static class UsuarioInfo {
    private Long id;
    private String username;
    private String nombre;
  }
}
