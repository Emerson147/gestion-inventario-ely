package com.emersondev.mapper;

import com.emersondev.api.request.TallaRequest;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Talla;
import org.springframework.stereotype.Component;

@Component
public class TallaMapper {

  public Talla toEntity(TallaRequest request) {
    if (request == null) {
      return null;
    }

    Talla talla = new Talla();
    talla.setNumero(request.getNumero());

    return talla;
  }

  public Talla toEntity(TallaRequest request, Color color) {
    Talla talla = toEntity(request);
    if (talla != null) {
      talla.setColor(color);
    }
    return talla;
  }

  public TallaResponse toResponse(Talla talla) {
    if (talla == null) {
      return null;
    }

    TallaResponse response = new TallaResponse();
    response.setId(talla.getId());
    response.setNumero(talla.getNumero());
    response.setCantidad(talla.getCantidad());

    return response;
  }
}
