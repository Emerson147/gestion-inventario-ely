package com.emersondev.domain.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VentaException extends BusinessException {

  public VentaException(String message) {
    super(message);
  }

  public VentaException(String message, Throwable cause) {
    super(message, cause);
  }
}