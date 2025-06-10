package com.emersondev.service.interfaces;

import com.emersondev.api.request.InventarioRequest;
import com.emersondev.api.request.MovimientoInventarioRequest;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.api.response.MovimientoInventarioResponse;
import com.emersondev.api.response.PagedResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface InventarioService {

  /**
   * Agrega un nuevo registro de inventario
   * @param request datos del inventario a agregar
   * @return respuesta con los datos del inventario creado
   */
  InventarioResponse agregarInventario(InventarioRequest request);

  /**
   * Obtiene todos los registros de inventario
   * @return lista de todo el inventario
   */
  PagedResponse<InventarioResponse> obtenerInventarios(Integer page, Integer size, String sortBy, String sortDir);

  /**
   * Obtiene un registro de inventario por su ID
   * @param id ID del inventario
   * @return datos del inventario
   */
  InventarioResponse obtenerInventarioPorId(Long id);

  /**
   * Obtiene un registro de inventario por su número de serie
   * @param serie número de serie del inventario
   * @return datos del inventario
   */
  InventarioResponse obtenerInventarioPorSerie(String serie);

  /**
   * Obtiene todos los registros de inventario de un producto específico
   * @param productoId ID del producto
   * @return lista de inventario del producto
   */
  List<InventarioResponse> obtenerInventarioPorProducto(Long productoId);

  /**
   * Transfiere inventario de un almacén a otro
   * @param inventarioId ID del inventario a transferir
   * @param almacenDestinoId ID del almacén destino
   * @param cantidad cantidad a transferir
   */
  void transferirInventario(Long inventarioId, Long almacenDestinoId, Integer cantidad);

  /**
   * Actualiza un registro de inventario
   * @param id ID del inventario a actualizar
   * @param request datos actualizados
   * @return inventario actualizado
   */
  InventarioResponse actualizarInventario(Long id, InventarioRequest request);

  /**
   * Elimina un registro de inventario
   * @param id ID del inventario a eliminar
   */
  void eliminarInventario(Long id);

  /**
   * Obtiene el stock total de un producto (sumando todos sus registros de inventario)
   * @param productoId ID del producto
   * @return cantidad total en stock
   */
  Integer obtenerStockTotalProducto(Long productoId);

  /**
   * Obtiene el stock para una variante específica de producto (color y talla)
   * @param productoId ID del producto
   * @param colorId ID del color
   * @param tallaId ID de la talla
   * @return cantidad en stock
   */
  Integer obtenerStockPorVariante(Long productoId, Long colorId, Long tallaId);

  /**
   * Actualiza el stock después de una venta
   * @param productoId ID del producto
   * @param colorId ID del color
   * @param tallaId ID de la talla
   * @param cantidad cantidad vendida
   */
  void actualizarStockPorVenta(Long productoId, Long colorId, Long tallaId, Integer cantidad, Long ventaId);

  /**
   * Obtiene el inventario con stock bajo
   * @param umbral nivel mínimo de stock
   * @return lista de inventario con stock bajo
   */
  List<InventarioResponse> obtenerInventarioConStockBajo(Integer umbral);

  /**
   * Disminuye el stock de un inventario específico
   * @param inventarioId ID del inventario
   * @param cantidad cantidad a disminuir
   */
  void disminuirStock(Long inventarioId, Integer cantidad);


  void devolverStockPorAnulacion(Long productoId, Long colorId, Long tallaId, Integer cantidad, Long ventaId);

  long contarProductosSinStock();

//  // Nuevos métodos para reportes
//  Map<String, Object> generarReporteStockActual();
//  Map<String, Object> generarReporteStockCritico(int stockMinimo);
//  Map<String, Object> generarReporteRotacionInventario(LocalDate fechaInicio, LocalDate fechaFin);
//  Map<String, Object> generarReporteValorInventario();
  
  
}

