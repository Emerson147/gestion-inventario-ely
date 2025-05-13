package com.emersondev.api.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MovimientoInventarioValidator implements ConstraintValidator<ValidMovimientoInventario, MovimientoInventarioRequest> {
  @Override
  public boolean isValid(MovimientoInventarioRequest value, ConstraintValidatorContext context) {
    if (value == null) return true; // Otro validador lo manejará

    boolean valid = true;
    context.disableDefaultConstraintViolation();

    // Validación condicional para TRASLADO
    if ("TRASLADO".equalsIgnoreCase(value.getTipo())) {
      if (value.getInventarioDestinoId() == null && value.getAlmacenDestinoId() == null) {
        valid = false;
        context.buildConstraintViolationWithTemplate(
                        "El ID del inventario destino o almacén destino es obligatorio para traslados")
                .addPropertyNode("inventarioDestinoId").addConstraintViolation();
      }
    }
    // Validación condicional para SALIDA-Venta
    if ("SALIDA".equalsIgnoreCase(value.getTipo())) {
      if (value.getVentaId() == null) {
        valid = false;
        context.buildConstraintViolationWithTemplate(
                        "El ID de la venta es obligatorio para salidas por venta")
                .addPropertyNode("ventaId").addConstraintViolation();
      }
    }
    // ¡Agrega más reglas según tus necesidades!

    return valid;
  }
}