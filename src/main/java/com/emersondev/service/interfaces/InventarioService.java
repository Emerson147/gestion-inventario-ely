package com.emersondev.service.interfaces;

import com.emersondev.api.request.InventarioRequest;
import com.emersondev.api.response.InventarioResponse;

import java.util.List;

public interface InventarioService {
  InventarioResponse agregarInventario(InventarioRequest inventarioRequest);

  InventarioResponse obtenerInventarioPorId(Long id);

  InventarioResponse obtenerInventarioPorSerie(String serie);

  List<InventarioResponse> obtenerInventarioPorProducto(Long productoId);

  List<InventarioResponse> obtenerTodoElInventario();

  InventarioResponse actualizarInventario(Long id, InventarioRequest inventarioRequest);

  void eliminarInventario(Long id);

  void transferirInventario(Long inventarioId, Long almacenDestinoId, Integer cantidad);

  List<InventarioResponse> obtenerInventarioConStockBajo(Integer umbral);

  Integer obtenerStockTotalProducto(Long productoId);

  Integer obtenerStockPorVariante(Long productoId, Long colorId, Long tallaId);
}
