package com.emersondev.api.request;

import com.emersondev.domain.entity.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VentaRequest {

  @NotNull(message = "El ID del cliente es obligatorio")
  private Long clienteId;

  @NotNull(message = "El ID del usuario es obligatorio")
  private Long usuarioId;

  @NotNull(message = "El tipo de comprobante es obligatorio")
  private Venta.TipoComprobante tipoComprobante;

  private String serieComprobante;

  private String numeroComprobante;

  @Size(max = 500, message = "Las observaciones no deben exceder los 500 caracteres")
  private String observaciones;

  @NotEmpty(message = "La venta debe tener al menos un detalle")
  @Valid
  private List<DetalleVentaRequest> detalles = new ArrayList<>();
}
