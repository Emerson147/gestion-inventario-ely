package com.emersondev.mapper;

import com.emersondev.api.request.ClienteRequest;
import com.emersondev.api.response.ClienteResponse;
import com.emersondev.domain.entity.Clientes;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

  /**
   * Convierte un ClienteRequest a una entidad Cliente
   */
  public Clientes toEntity(ClienteRequest request) {
    if (request == null) {
      return null;
    }

    Clientes cliente = new Clientes();
    cliente.setNombres(request.getNombres());
    cliente.setApellidos(request.getApellidos());
    cliente.setDni(request.getDni());
    cliente.setRuc(request.getRuc());
    cliente.setTelefono(request.getTelefono());
    cliente.setDireccion(request.getDireccion());
    cliente.setEmail(request.getEmail());

    return cliente;
  }

  /**
   * Convierte una entidad Cliente a un ClienteResponse
   */
  public ClienteResponse toResponse(Clientes cliente) {
    if (cliente == null) {
      return null;
    }

    ClienteResponse response = new ClienteResponse();
    response.setId(cliente.getId());
    response.setNombres(cliente.getNombres());
    response.setApellidos(cliente.getApellidos());
    response.setNombreCompleto(cliente.getNombres() + " " + cliente.getApellidos());
    response.setDni(cliente.getDni());
    response.setRuc(cliente.getRuc());
    response.setTelefono(cliente.getTelefono());
    response.setDireccion(cliente.getDireccion());
    response.setEmail(cliente.getEmail());
    response.setEstado(cliente.getEstado());
    response.setFechaCreacion(cliente.getFechaCreacion());
    response.setFechaActualizacion(cliente.getFechaActualizacion());

    return response;
  }

}