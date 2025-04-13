package com.emersondev.service.interfaces;

import com.emersondev.api.request.ClienteRequest;
import com.emersondev.api.response.ClienteResponse;
import com.emersondev.api.response.PagedResponse;

public interface ClienteService {
  ClienteResponse crearCliente(ClienteRequest clienteRequest);

  ClienteResponse obtenerClientePorId(Long id);

  ClienteResponse obtenerClientePorDocumento(String documento);

  PagedResponse<ClienteResponse> obtenerClientes(int page, int size, String sortBy, String sortDir);

  PagedResponse<ClienteResponse> buscarClientes(String termino, int page, int size);

  ClienteResponse actualizarCliente(Long id, ClienteRequest clienteRequest);

  void eliminarCliente(Long id);

}
