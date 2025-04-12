package com.emersondev.api.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClienteResponse {
  private Long id;
  private String nombre;
  private String apellido;
  private String documento;
  private String email;
  private String telefono;
  private String direccion;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
}
