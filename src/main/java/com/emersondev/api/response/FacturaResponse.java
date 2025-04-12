package com.emersondev.api.response;

import com.emersondev.domain.entity.Factura;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacturaResponse {
  private Long id;
  private String numeroFactura;
  private Long ventaId;
  private Factura.TipoComprobante tipoComprobante;
  private LocalDateTime fechaEmision;
}
