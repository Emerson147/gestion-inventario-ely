package com.emersondev.api.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UsuarioResponse {
  private Long id;
  private String nombres;
  private String apellidos;
  private String username;
  private String email;
  private boolean activo;
  private Set<String> roles;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
