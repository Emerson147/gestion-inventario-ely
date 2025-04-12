package com.emersondev.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VentaRequest {

  @NotNull(message = "El ID del cliente es obligatorio")
  private Long clienteId;

  @NotEmpty(message = "La venta debe tener al menos un detalle")
  @Valid
  private List<DetalleVentaRequest> detalles;
}
