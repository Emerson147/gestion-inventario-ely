package com.emersondev.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenRequest {
  /**
   * Nombre del almacén
   */
  @NotBlank(message = "El nombre del almacén es obligatorio")
  private String nombre;

  /**
   * Ubicación del almacén
   */
  private String ubicacion;

  /**
   * Descripción del almacén
   */
  private String descripcion;
}
