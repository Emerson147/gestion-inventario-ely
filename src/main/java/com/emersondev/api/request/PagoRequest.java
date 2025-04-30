package com.emersondev.api.request;

import com.emersondev.domain.entity.Pago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PagoRequest {
  @NotNull(message = "El ID de la venta es obligatorio")
  private Long ventaId;

  @NotNull(message = "El ID del usuario es obligatorio")
  private Long usuarioId;

  @NotNull(message = "El monto es obligatorio")
  @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
  private BigDecimal monto;

  @NotNull(message = "El método de pago es obligatorio")
  private Pago.MetodoPago metodoPago;

  @Size(max = 100, message = "El número de referencia no debe exceder los 100 caracteres")
  private String numeroReferencia;

  @Size(max = 100, message = "El nombre en la tarjeta no debe exceder los 100 caracteres")
  private String nombreTarjeta;

  @Size(max = 20, message = "Los últimos 4 dígitos no deben exceder los 20 caracteres")
  private String ultimos4Digitos;

  @Size(max = 500, message = "Las observaciones no deben exceder los 500 caracteres")
  private String observaciones;
}
