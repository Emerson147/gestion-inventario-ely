package com.emersondev.mapper;

import com.emersondev.api.response.MovimientoInventarioResponse;
import com.emersondev.domain.entity.Inventario;
import com.emersondev.domain.entity.MovimientoInventario;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades MovimientoInventario a DTOs de respuesta.
 * Incluye soporte para traslados inteligentes, agregando información del inventario destino si corresponde.
 */
@Component
public class MovimientoInventarioMapper {
  /**
   * Convierte una entidad MovimientoInventario a MovimientoInventarioResponse.
   * Si el movimiento es un traslado, intenta poblar los datos del inventario destino si están disponibles.
   * @param movimiento entidad a convertir
   * @return objeto MovimientoInventarioResponse
   */
  public MovimientoInventarioResponse toResponse(MovimientoInventario movimiento) {
    if (movimiento == null) {
      return null;
    }

    MovimientoInventarioResponse response = new MovimientoInventarioResponse();
    response.setId(movimiento.getId());

    Inventario inventario = movimiento.getInventario();
    if (inventario != null) {
      response.setInventarioId(inventario.getId());
      response.setEstadoResultante(inventario.getEstado());

      // Mapear producto
      if (inventario.getProducto() != null) {
        MovimientoInventarioResponse.ProductoResponse productoResponse = new MovimientoInventarioResponse.ProductoResponse();
        productoResponse.setId(inventario.getProducto().getId());
        productoResponse.setCodigo(inventario.getProducto().getCodigo());
        productoResponse.setNombre(inventario.getProducto().getNombre());
        response.setProducto(productoResponse);
      }

      // Mapear color
      if (inventario.getColor() != null) {
        MovimientoInventarioResponse.ColorResponse colorResponse = new MovimientoInventarioResponse.ColorResponse();
        colorResponse.setId(inventario.getColor().getId());
        colorResponse.setNombre(inventario.getColor().getNombre());
        response.setColor(colorResponse);
      }

      // Mapear talla
      if (inventario.getTalla() != null) {
        MovimientoInventarioResponse.TallaResponse tallaResponse = new MovimientoInventarioResponse.TallaResponse();
        tallaResponse.setId(inventario.getTalla().getId());
        tallaResponse.setNumero(inventario.getTalla().getNumero());
        response.setTalla(tallaResponse);
      }
    }

    // Mapear resto de campos
    response.setCantidad(movimiento.getCantidad());
    response.setTipo(movimiento.getTipo());
    response.setDescripcion(movimiento.getDescripcion());
    response.setReferencia(movimiento.getReferencia());
    response.setUsuario(movimiento.getUsuario());
    response.setFechaMovimiento(movimiento.getFechaMovimiento());

    // --- SOPORTE PARA TRASLADOS INTELIGENTES ---
    // Si el movimiento es un TRASLADO, intenta poblar los datos de inventario destino
    if (movimiento.getTipo() == MovimientoInventario.TipoMovimiento.TRASLADO) {
      // Suponiendo que guardas el ID del destino en la descripción o en algún atributo adicional,
      // o que tienes una relación directa, ajústalo a tu modelo.
      if (movimiento.getInventarioDestino() != null) {
        Inventario inventarioDestino = movimiento.getInventarioDestino();
        response.setInventarioDestinoId(inventarioDestino.getId());
        if (inventarioDestino.getAlmacen() != null) {
          response.setAlmacenDestinoNombre(inventarioDestino.getAlmacen().getNombre());
        }
        // Puedes mapear más datos del inventario destino si lo deseas
      }
    }

    return response;
  }
}