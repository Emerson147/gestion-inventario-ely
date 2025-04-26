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

  @NotBlank(message = "Los nombres son obligatorios")
  @Size(max = 100, message = "Los nombres no deben exceder los 100 caracteres")
  private String nombres;

  @NotBlank(message = "Los apellidos son obligatorios")
  @Size(max = 100, message = "Los apellidos no deben exceder los 100 caracteres")
  private String apellidos;

  @Size(max = 15, message = "El DNI no debe exceder los 15 caracteres")
  @Pattern(regexp = "^[0-9]*$", message = "El DNI solo debe contener números")
  private String dni;

  @Size(max = 15, message = "El RUC no debe exceder los 15 caracteres")
  @Pattern(regexp = "^[0-9]*$", message = "El RUC solo debe contener números")
  private String ruc;

  @Size(max = 15, message = "El teléfono no debe exceder los 15 caracteres")
  private String telefono;

  @Size(max = 255, message = "La dirección no debe exceder los 255 caracteres")
  private String direccion;

  @Email(message = "El formato del email no es válido")
  @Size(max = 100, message = "El email no debe exceder los 100 caracteres")
  private String email;
}
