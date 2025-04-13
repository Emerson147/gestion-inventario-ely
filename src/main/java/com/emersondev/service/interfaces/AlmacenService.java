package com.emersondev.service.interfaces;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.api.response.InventarioResponse;

import java.util.List;

public interface AlmacenService {
  AlmacenResponse crearAlmacen(AlmacenRequest almacenRequest);

  AlmacenResponse obtenerAlmacenPorId(Long id);

  List<AlmacenResponse> obtenerTodosLosAlmacenes();

  List<InventarioResponse> obtenerInventarioPorAlmacen(Long almacenId);

  AlmacenResponse actualizarAlmacen(Long id, AlmacenRequest almacenRequest);

  void eliminarAlmacen(Long id);
}
