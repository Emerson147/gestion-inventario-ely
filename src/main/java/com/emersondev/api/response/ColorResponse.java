package com.emersondev.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ColorResponse {

  private Long id;
  private String nombre;
  private String codigoHex; // 👈 NUEVO CAMPO
  private List<TallaResponse> tallas;

}
