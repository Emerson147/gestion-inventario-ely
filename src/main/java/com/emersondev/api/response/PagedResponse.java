package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {

  private List<T> contenido;
  private int numeroPagina;
  private int tamañoPagina;
  private long totalElementos;
  private int totalPaginas;
  private boolean ultima;



}
