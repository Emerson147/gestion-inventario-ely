package com.emersondev.service.interfaces;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.api.response.InventarioResponse;

import java.util.List;

public interface AlmacenService {


  /**
   * Obtiene todos los almacenes del sistema
   * @return lista de almacenes
   */
  List<AlmacenResponse> obtenerTodosLosAlmacenes();

  /**
   * Obtiene un almacén por su ID
   * @param id ID del almacén
   * @return datos del almacén
   */
  AlmacenResponse obtenerAlmacenPorId(Long id);

  /**
   * Obtiene un almacén por su nombre
   * @param nombre nombre del almacén
   * @return datos del almacén
   */
  AlmacenResponse obtenerAlmacenPorNombre(String nombre);

  /**
   * Obtiene todo el inventario asociado a un almacén
   * @param id ID del almacén
   * @return lista de inventario
   */
  List<InventarioResponse> obtenerInventarioPorAlmacen(Long id);

  /**
   * Crea un nuevo almacén
   * @param almacenRequest datos del almacén a crear
   * @return datos del almacén creado
   */
  AlmacenResponse crearAlmacen(AlmacenRequest almacenRequest);

  /**
   * Actualiza un almacén existente
   * @param id ID del almacén a actualizar
   * @param almacenRequest nuevos datos del almacén
   * @return datos del almacén actualizado
   */
  AlmacenResponse actualizarAlmacen(Long id, AlmacenRequest almacenRequest);

  /**
   * Elimina un almacén
   * @param id ID del almacén a eliminar
   */
  void eliminarAlmacen(Long id);

  /**
   * Verifica si existe un almacén con el nombre especificado
   * @param nombre nombre del almacén
   * @return true si existe, false en caso contrario
   */
  boolean existePorNombre(String nombre);

  /**
   * Verifica si un almacén tiene inventario asociado
   * @param id ID del almacén
   * @return true si tiene inventario, false en caso contrario
   */
  boolean tieneInventarioAsociado(Long id);

}
