package com.emersondev.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorRequest {

  /**
   * Nombre del color del producto
   */
  @NotBlank(message = "El nombre del color es obligatorio")
  @Size(min = 2, max = 50, message = "El nombre del color debe tener entre 2 y 50 caracteres")
  private String nombre;

  @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El código HEX debe tener el formato #RRGGBB")
  private String codigoHex; // 👈 NUEVO CAMPO
  /**
    Tallas disponibles para este color
   **/
  @Valid
  private List<TallaRequest> tallas;

}