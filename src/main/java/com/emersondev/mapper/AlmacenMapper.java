package com.emersondev.mapper;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.domain.entity.Almacen;
import org.springframework.stereotype.Component;

@Component
public class AlmacenMapper {


  /**
   * Convierte un AlmacenRequest a una entidad Almacen
   */
  public Almacen toEntity(AlmacenRequest request) {
    if (request == null) {
      return null;
    }

    Almacen almacen = new Almacen();
    almacen.setNombre(request.getNombre());
    almacen.setUbicacion(request.getUbicacion());
    almacen.setDescripcion(request.getDescripcion());

    return almacen;

  }

  /**
   * Convierte una entidad Almacen a un AlmacenResponse
   */
  public AlmacenResponse toResponse(Almacen almacen) {
    if (almacen == null) {
      return null;
    }

    AlmacenResponse response = new AlmacenResponse();
    response.setId(almacen.getId());
    response.setNombre(almacen.getNombre());
    response.setUbicacion(almacen.getUbicacion());
    response.setDescripcion(almacen.getDescripcion());
    response.setFechaCreacion(almacen.getFechaCreacion());
    response.setFechaActualizacion(almacen.getFechaActualizacion());

    return response;
  }

}
