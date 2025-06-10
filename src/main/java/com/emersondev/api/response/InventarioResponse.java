package com.emersondev.api.response;

import com.emersondev.domain.entity.Inventario;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta para operaciones de inventario
 */
@Data
public class InventarioResponse {


  private Long id;
  private String serie;
  private ProductoSimpleResponse producto;
  private ColorSimpleResponse color;
  private TallaSimpleResponse talla;
  private AlmacenSimpleResponse almacen;
  private Integer cantidad;
  private Inventario.EstadoInventario estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  /**
   * Respuesta simplificada para un producto
   */
  @Data
  public static class ProductoSimpleResponse {
    private Long id;
    private String codigo;
    private String nombre;
  }

  /**
   * Respuesta simplificada para un color
   */
  @Data
  public static class ColorSimpleResponse {
    private Long id;
    private String nombre;
  }

  /**
   * Respuesta simplificada para una talla
   */
  @Data
  public static class TallaSimpleResponse {
    private Long id;
    private String numero;
  }

  /**
   * Respuesta simplificada para un almacén
   */
  @Data
  public static class AlmacenSimpleResponse {
    private Long id;
    private String nombre;
    private String ubicacion;
  }
}
