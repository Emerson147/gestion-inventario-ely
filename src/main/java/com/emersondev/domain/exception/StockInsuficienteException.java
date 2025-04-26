package com.emersondev.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockInsuficienteException extends InventarioException {

  private final int stockDisponible;
  private final int stockSolicitado;

  public StockInsuficienteException(String message, int stockDisponible, int stockSolicitado) {
    super(message);
    this.stockDisponible = stockDisponible;
    this.stockSolicitado = stockSolicitado;
  }

}