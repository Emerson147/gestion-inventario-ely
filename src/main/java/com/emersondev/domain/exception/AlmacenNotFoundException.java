package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlmacenNotFoundException  extends ResourceNotFoundException {

  public AlmacenNotFoundException(Long id) {
    super("Almacén", "id", id);
  }

  public AlmacenNotFoundException(String nombre) {
    super("Almacén", "nombre", nombre);
  }
}
