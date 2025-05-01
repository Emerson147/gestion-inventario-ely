package com.emersondev.api.request;

import com.emersondev.domain.entity.Comprobante;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRequest {

  @NotNull(message = "El ID de la venta es obligatorio")
  private Long ventaId;

  @NotNull(message = "El tipo de documento es obligatorio")
  private Comprobante.TipoDocumento tipoDocumento;

  @Size(max = 10, message = "La serie no debe exceder los 10 caracteres")
  private String serie;

  @Size(max = 500, message = "Las observaciones no deben exceder los 500 caracteres")
  private String observaciones;
}
