package com.emersondev.api.response;

import com.emersondev.domain.entity.Inventario;
import com.emersondev.domain.entity.MovimientoInventario;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta de un movimiento de inventario, incluyendo información relevante
 * del inventario origen, y opcionalmente, del inventario destino en caso de traslado.
 */
@Data
public class MovimientoInventarioResponse {

  /**
   * ID del movimiento de inventario.
   */
  private Long id;

  /**
   * ID del inventario ORIGEN.
   */
  private Long inventarioId;

  /**
   * Información del producto relacionado.
   */
  private ProductoResponse producto;

  /**
   * Información del color relacionado.
   */
  private ColorResponse color;

  /**
   * Información de la talla relacionada.
   */
  private TallaResponse talla;

  /**
   * Cantidad del movimiento.
   */
  private Integer cantidad;

  /**
   * Tipo de movimiento (ENTRADA, SALIDA, AJUSTE, TRASLADO).
   */
  private MovimientoInventario.TipoMovimiento tipo;

  /**
   * Descripción o motivo del movimiento.
   */
  private String descripcion;

  /**
   * Referencia del movimiento (puede ser código de venta, guía, etc).
   */
  private String referencia;

  /**
   * Usuario que realizó el movimiento.
   */
  private String usuario;

  /**
   * Fecha y hora en que se realizó el movimiento.
   */
  private LocalDateTime fechaMovimiento;

  /**
   * Estado resultante del inventario después del movimiento.
   */
  private Inventario.EstadoInventario estadoResultante;

  // --------- NUEVO: Campos para traslados inteligentes ---------

  /**
   * ID del inventario DESTINO (solo para traslados, puede ser null para otros tipos).
   */
  private Long inventarioDestinoId;

  /**
   * Nombre del almacén destino (opcional, solo para traslados).
   */
  private String almacenDestinoNombre;

  // Puedes agregar más detalles del inventario destino si lo requieres, por ejemplo producto/color/talla destino,
  // aunque normalmente en un traslado solo cambia el almacén.

  // ------------------------------------------------------------

  @Data
  public static class ProductoResponse {
    private Long id;
    private String codigo;
    private String nombre;
  }

  @Data
  public static class ColorResponse {
    private Long id;
    private String nombre;
  }

  @Data
  public static class TallaResponse {
    private Long id;
    private String numero;
  }
}