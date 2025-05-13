package com.emersondev.api.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MovimientoInventarioValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMovimientoInventario {
  String message() default "Datos inválidos para el movimiento";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}