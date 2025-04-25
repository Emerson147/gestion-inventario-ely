package com.emersondev.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenRequest {

  @NotBlank(message = "El nombre es obligatorio")
  @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
  private String nombre;

  @Size(max = 255, message = "La ubicación no debe exceder los 255 caracteres")
  private String ubicacion;

  @Size(max = 500, message = "La descripción no debe exceder los 500 caracteres")
  private String descripcion;

}
