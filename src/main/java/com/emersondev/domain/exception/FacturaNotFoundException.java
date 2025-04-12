package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FacturaNotFoundException extends ResourceNotFoundException {

  public FacturaNotFoundException(Long id) {
    super("Factura", "id", id);
  }

  public FacturaNotFoundException(String numeroFactura) {
    super("Factura", "número de factura", numeroFactura);
  }
}