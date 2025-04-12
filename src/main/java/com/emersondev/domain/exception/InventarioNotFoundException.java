package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InventarioNotFoundException extends ResourceNotFoundException{

  public InventarioNotFoundException(Long id) {
    super("Inventario", "id", id);
  }

  public InventarioNotFoundException(String serie) {
    super("Inventario", "serie", serie);
  }

}
