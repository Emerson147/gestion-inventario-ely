package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductoNotFoundException extends ResourceNotFoundException {

  public ProductoNotFoundException(Long id) {
    super("Producto", "id", id);
  }

  public ProductoNotFoundException(String codigo) {
    super("Producto", "código", codigo);
  }
}
