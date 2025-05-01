package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResponse {

  private Long id;
  private String tipoDocumento;
  private String serie;
  private String numero;
  private LocalDateTime fechaEmision;
  private String codigoHash;
  private VentaInfo venta;
  private ClienteInfo cliente;
  private UsuarioInfo usuario;
  private BigDecimal subtotal;
  private BigDecimal igv;
  private BigDecimal total;
  private String estado;
  private String observaciones;
  private List<DetalleComprobanteResponse> detalles = new ArrayList<>();
  private String rutaArchivoPdf;
  private String rutaArchivoXml;
  private LocalDateTime fechaAnulacion;
  private String motivoAnulacion;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  @Data
  public static class VentaInfo {
    private Long id;
    private String numeroVenta;
    private String estado;
  }

  @Data
  public static class ClienteInfo {
    private Long id;
    private String nombres;
    private String apellidos;
    private String dni;
    private String ruc;
    private String direccion;
  }

  @Data
  public static class UsuarioInfo {
    private Long id;
    private String nombre;
    private String username;
  }
}
