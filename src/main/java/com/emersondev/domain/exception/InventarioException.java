package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InventarioException extends BusinessException {

  public InventarioException(String message) {
    super(message);
  }

  public InventarioException(String message, Throwable cause) {
    super(message, cause);
  }
}
