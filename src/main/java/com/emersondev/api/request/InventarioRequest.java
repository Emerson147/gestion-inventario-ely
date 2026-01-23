package com.emersondev.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioRequest {

  @NotNull(message = "El ID del producto es obligatorio")
  private Long productoId;

  @NotNull(message = "El ID del color es obligatorio")
  private Long colorId;

  @NotNull(message = "El ID de la talla es obligatorio")
  private Long tallaId;

  @NotNull(message = "El ID del almacén es obligatorio")
  private Long almacenId;

  @NotNull(message = "La cantidad es obligatoria")
  @PositiveOrZero(message = "La cantidad no puede ser negativa")
  private Integer cantidad;

  private String serie;

  private String ubicacionExacta;

}
