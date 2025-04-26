package com.emersondev.api.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta para operaciones de cliente
 */
@Data
public class ClienteResponse {
  private Long id;
  private String nombres;
  private String apellidos;
  private String nombreCompleto;
  private String dni;
  private String ruc;
  private String telefono;
  private String direccion;
  private String email;
  private Boolean estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

}
