package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear un recurso duplicado
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException{
  public DuplicateResourceException(String resource, String field, String value) {
    super("Ya existe un " + resource + " con el campo " + field + ": " + value);
  }
}
