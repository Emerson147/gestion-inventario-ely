package com.emersondev.api.request;

import jakarta.validation.constraints.Min;
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

  @NotNull(message = "El ID del producto es obligatorio")
  private Long productoId;

  @NotNull(message = "El ID del color es obligatorio")
  private Long colorId;

  @NotNull(message = "El ID de la talla es obligatorio")
  private Long tallaId;

  @NotNull(message = "La cantidad es obligatoria")
  @Min(value = 1, message = "La cantidad debe ser al menos 1")
  private Integer cantidad;
}
