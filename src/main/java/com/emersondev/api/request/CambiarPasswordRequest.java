package com.emersondev.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {
  /**
   * Contraseña actual del usuario
   */
  @NotBlank(message = "La contraseña actual es obligatoria")
  private String passwordActual;

  /**
   *  Nueva contraseña del usuario
   */
  @NotBlank(message = "La nueva contraseña es obligatoria")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
          message = "La contraseña debe contener al menos: un número, una minúscula, una mayúscula y un carácter especial")
  private String nuevaPassword;

  /**
   * Confirmación de la nueva contraseña
   */
  @NotBlank(message = "La confirmación de la contraseña es obligatoria")
  private String confirmarPassword;
  }