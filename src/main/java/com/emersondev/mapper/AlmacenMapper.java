package com.emersondev.mapper;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.domain.entity.Almacen;
import org.springframework.stereotype.Component;

@Component
public class AlmacenMapper {
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

  public AlmacenResponse toResponse(Almacen almacen) {
    if (almacen == null) {
      return null;
    }

    AlmacenResponse response = new AlmacenResponse();
    response.setId(almacen.getId());
    response.setNombre(almacen.getNombre());
    response.setUbicacion(almacen.getUbicacion());
    response.setDescripcion(almacen.getDescripcion());

    return response;
  }
}
