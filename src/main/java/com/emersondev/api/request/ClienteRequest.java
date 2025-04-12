package com.emersondev.api.request;

import jakarta.validation.constraints.Email;
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
public class ClienteRequest {
  /**
   * * Nombre del cliente
   */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
  private String nombre;

  /**
   * Apellido del cliente
   */
  @NotBlank(message = "El apellido es obligatorio")
  @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
  private String apellido;

  /**
   * Documento identificativo del cliente (DNI, NIE, etc.)
   */
  @NotBlank(message = "El documento es obligatorio")
  @Pattern(regexp = "^[0-9A-Z]{5,15}$", message = "El documento debe contener entre 5 y 15 caracteres alfanuméricos")
  private String documento;

  /**
   * Correo electrónico del cliente
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email debe ser válido")
  private String email;

  /**
   * Número de teléfono del cliente
   */
  @Pattern(regexp = "^[0-9]{9,15}$", message = "El teléfono debe contener entre 9 y 15 dígitos")
  private String telefono;

  /**
   * Dirección física del cliente
   */
  @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
  private String direccion;
}
