package com.emersondev.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {
  /**
     * Nombre del usuario
     */
  @NotBlank(message = "El nombre es obligatorio")
  @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
  private String nombre;

  /** * Apellido del usuario*/
  @NotBlank(message = "El apellido es obligatorio")
  @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
  private String apellido;

  /**
   * Nombre de usuario para la autenticación
   */
  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
  private String username;

  /**
   * Correo electrónico del usuario
   */
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email debe ser válido")
  private String email;

  /**
   * Contraseña del usuario (opcional para actualizaciones)
   */
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "La contraseña debe contener al menos: un número, una minúscula, una mayúscula y un carácter especial")
  @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
  private String password;

  /**
   * Roles asignados al usuario
   */
  private Set<String> roles;

 }



