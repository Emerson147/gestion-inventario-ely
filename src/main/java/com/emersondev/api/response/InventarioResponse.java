package com.emersondev.api.response;

import lombok.Data;

@Data
public class InventarioResponse {

  private Long id;
  private ProductoResponse producto;
  private ColorResponse color;
  private TallaResponse talla;
  private AlmacenResponse almacen;
  private Integer cantidad;
  private String serie;
  private String ubicacionExacta;
}
