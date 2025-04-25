package com.emersondev.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlmacenResponse {
  private Long id;
  private String nombre;
  private String ubicacion;
  private String descripcion;

  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="America/Lima")
  private LocalDateTime fechaCreacion;

  @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="America/Lima")
  private LocalDateTime fechaActualizacion;
}
