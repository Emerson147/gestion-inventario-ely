package com.emersondev.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidMovimientoInventario
public class MovimientoInventarioRequest {

  /**
   * ID del inventario al que se aplicará el movimiento
   */
  @NotNull(message = "El ID del inventario es obligatorio")
  private Long inventarioId;

  /**
   * ID del inventario destino (solo obligatorio para TRASLADO, validado condicionalmente)
   */
  private Long inventarioDestinoId;

  /**
   * Si inventarioDestinoId es null y el movimiento es TRASLADO, se usan estos campos para crearlo o buscarlo:
   */
  private Long productoId;
  private Long colorId;
  private Long tallaId;
  private Long almacenDestinoId;

  /**
   * ID de la venta asociada al movimiento (solo obligatorio para SALIDA-venta, validado condicionalmente)
   */
  private Long ventaId;

  /**
   * Cantidad del movimiento
   */
  @NotNull(message = "La cantidad es obligatoria")
  @Min(value = 1, message = "La cantidad debe ser mayor a cero")
  private Integer cantidad;

  /**
   * Tipo de movimiento (ENTRADA, SALIDA, AJUSTE, TRASLADO)
   */
  @NotNull(message = "El tipo de movimiento es obligatorio")
  private String tipo;

  /**
   * Descripción o motivo del movimiento
   */
  @NotBlank(message = "La descripción es obligatoria")
  @Size(min = 5, max = 255, message = "La descripción debe tener entre 5 y 255 caracteres")
  private String descripcion;

  /**
   * Referencia del movimiento
   */
  @NotBlank(message = "La referencia es obligatoria")
  @Size(min = 5, max = 50, message = "La referencia debe tener entre 5 y 50 caracteres")
  private String referencia;

  /**
   * Usuario que realiza el movimiento
   */
  @NotBlank(message = "El usuario es obligatorio")
  @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
  private String usuario;
}