package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PagoNotFoundException extends RuntimeException{


  public PagoNotFoundException(Long id) {
    super("No se encontró el pago con ID: " + id);
  }

  public PagoNotFoundException(String campo, String valor) {
    super("No se encontró el pago con " + campo + ": " + valor);
  }
}
