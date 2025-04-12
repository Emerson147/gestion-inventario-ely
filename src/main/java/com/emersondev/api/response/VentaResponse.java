package com.emersondev.api.response;

import com.emersondev.domain.entity.Venta;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponse {
  private Long id;
  private String numeroVenta;
  private ClienteResponse cliente;
  private UsuarioResponse usuario;
  private List<DetalleVentaResponse> detalles;
  private BigDecimal subtotal;
  private BigDecimal igv;
  private BigDecimal total;
  private Venta.EstadoVenta estado;
  private FacturaResponse factura;
  private LocalDateTime fechaCreacion;
}
