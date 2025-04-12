package com.emersondev.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRequest {

  @NotNull(message = "El ID del inventario es obligatorio")
  private Long inventarioId;

  @NotNull(message = "La cantidad es obligatoria")
  @Positive(message = "La cantidad debe ser positiva")
  private Integer cantidad;
}
