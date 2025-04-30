package com.emersondev.api.response;

import com.emersondev.domain.entity.Pago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

  private Long id;
  private String numeroPago;
  private VentaInfo venta;
  private UsuarioInfo usuario;
  private BigDecimal monto;
  private String metodoPago;
  private String estado;
  private String numeroReferencia;
  private String nombreTarjeta;
  private String ultimos4Digitos;
  private String observaciones;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  @Data
  public static class VentaInfo {
    private Long id;
    private String numeroVenta;
    private String cliente;
    private BigDecimal total;
    private BigDecimal saldoPendiente;
    private String estado;
  }

  @Data
  public static class UsuarioInfo {
    private Long id;
    private String nombre;
    private String username;
  }
}
