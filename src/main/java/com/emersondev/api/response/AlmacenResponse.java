package com.emersondev.api.response;

import lombok.Data;

@Data
public class AlmacenResponse {
  private Long id;
  private String nombre;
  private String ubicacion;
  private String descripcion;
}
