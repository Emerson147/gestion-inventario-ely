package com.emersondev.service.interfaces;

import com.emersondev.api.request.ClienteRequest;
import com.emersondev.api.response.ClienteResponse;
import com.emersondev.api.response.PagedResponse;

import java.util.List;

public interface ClienteService {


  /**
   * Obtiene todos los clientes
   * @return lista de clientes
   */
  List<ClienteResponse> obtenerTodosLosClientes();

  /**
   * Obtiene clientes activos
   * @return lista de clientes activos
   */
  List<ClienteResponse> obtenerClientesActivos();

  /**
   * Obtiene un cliente por su ID
   * @param id ID del cliente
   * @return datos del cliente
   */
  ClienteResponse obtenerClientePorId(Long id);

  /**
   * Busca clientes por DNI, RUC, nombres o apellidos
   * @param termino término de búsqueda
   * @return lista de clientes que coinciden con el término
   */
  List<ClienteResponse> buscarClientes(String termino);

  /**
   * Obtiene un cliente por su DNI
   * @param dni DNI del cliente
   * @return datos del cliente
   */
  ClienteResponse obtenerClientePorDni(String dni);

  /**
   * Obtiene un cliente por su RUC
   * @param ruc RUC del cliente
   * @return datos del cliente
   */
  ClienteResponse obtenerClientePorRuc(String ruc);

  /**
   * Obtiene un cliente por su email
   * @param email Email del cliente
   * @return datos del cliente
   */
  ClienteResponse obtenerClientePorEmail(String email);

  /**
   * Crea un nuevo cliente
   * @param clienteRequest datos del cliente a crear
   * @return datos del cliente creado
   */
  ClienteResponse crearCliente(ClienteRequest clienteRequest);

  /**
   * Actualiza un cliente existente
   * @param id ID del cliente a actualizar
   * @param clienteRequest nuevos datos del cliente
   * @return datos del cliente actualizado
   */
  ClienteResponse actualizarCliente(Long id, ClienteRequest clienteRequest);

  /**
   * Desactiva un cliente
   * @param id ID del cliente a desactivar
   */
  void desactivarCliente(Long id);

  /**
   * Reactiva un cliente
   * @param id ID del cliente a reactivar
   */
  void reactivarCliente(Long id);

  /**
   * Elimina un cliente
   * @param id ID del cliente a eliminar
   */
  void eliminarCliente(Long id);

  /**
   * Verifica si existe un cliente con el DNI especificado
   * @param dni DNI a verificar
   * @return true si existe, false en caso contrario
   */
  boolean existePorDni(String dni);

  /**
   * Verifica si existe un cliente con el RUC especificado
   * @param ruc RUC a verificar
   * @return true si existe, false en caso contrario
   */
  boolean existePorRuc(String ruc);

  /**
   * Verifica si existe un cliente con el email especificado
   * @param email Email a verificar
   * @return true si existe, false en caso contrario
   */
  boolean existePorEmail(String email);

  /**
   * Verifica si un cliente tiene ventas asociadas
   * @param id ID del cliente
   * @return true si tiene ventas, false en caso contrario
   */
  boolean tieneVentasAsociadas(Long id);


}
