package com.emersondev.api.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UsuarioResponse {
  private Long id;
  private String nombre;
  private String apellido;
  private String username;
  private String email;
  private boolean activo;
  private Set<String> roles;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
