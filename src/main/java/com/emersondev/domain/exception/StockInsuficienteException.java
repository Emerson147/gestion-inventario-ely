package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockInsuficienteException extends InventarioException {

  public StockInsuficienteException(String message) {
    super(message);
  }

  public StockInsuficienteException(String serie, Integer cantidadSolicitada, Integer cantidadDisponible) {
    super(String.format(
            "Stock insuficiente para el producto con serie '%s'. Solicitado: %d, Disponible: %d",
            serie, cantidadSolicitada, cantidadDisponible));
  }
}