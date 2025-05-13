package com.emersondev.service.interfaces;

import com.emersondev.api.request.MovimientoInventarioRequest;
import com.emersondev.api.response.MovimientoInventarioResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.domain.entity.MovimientoInventario;

import java.time.LocalDateTime;

public interface MovimientoInventarioService {
  /**
   * Registra un nuevo movimiento de inventario
   * @param movimientoRequest datos del movimiento
   * @return información del movimiento registrado
   */
  MovimientoInventarioResponse registrarMovimiento(MovimientoInventarioRequest movimientoRequest);

  /**
   * Obtiene los movimientos de un inventario específico
   * @param inventarioId ID del inventario
   * @param page número de página
   * @param size tamaño de página
   * @return movimientos paginados
   */
  PagedResponse<MovimientoInventarioResponse> obtenerMovimientosPorInventario(
          Long inventarioId, int page, int size);

  /**
   * Obtiene un movimiento específico por su ID
   * @param id ID del movimiento
   * @return información del movimiento
   */
  MovimientoInventarioResponse obtenerMovimientoPorId(Long id);

  /**
   * Busca movimientos con filtros
   * @param inventarioId ID del inventario (opcional)
   * @param productoId ID del producto (opcional)
   * @param colorId ID del color (opcional)
   * @param tallaId ID de la talla (opcional)
   * @param tipo tipo de movimiento (opcional)
   * @param fechaInicio fecha inicial para filtrar (opcional)
   * @param fechaFin fecha final para filtrar (opcional)
   * @param page número de página
   * @param size tamaño de página
   * @param sortBy campo para ordenar
   * @param sortDir dirección de ordenamiento
   * @return movimientos paginados que cumplen con los filtros
   */
  PagedResponse<MovimientoInventarioResponse> buscarMovimientos(
          Long inventarioId, Long productoId, Long colorId, Long tallaId,
          MovimientoInventario.TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin,
          int page, int size, String sortBy, String sortDir);
}
