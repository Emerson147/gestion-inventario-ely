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
public class TransferenciaInventarioRequest {

  @NotNull(message = "El ID del inventario origen es obligatorio")
  private Long inventarioId;

  @NotNull(message = "El ID del almacén destino es obligatorio")
  private Long almacenDestinoId;

  @NotNull(message = "La cantidad a transferir es obligatoria")
  @Min(value = 1, message = "La cantidad debe ser mayor a cero")
  private Integer cantidad;

}
