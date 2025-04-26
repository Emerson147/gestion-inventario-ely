package com.emersondev.mapper;

import com.emersondev.api.request.ColorRequest;
import com.emersondev.api.request.TallaRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.entity.Talla;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ColorMapper {
  /**
   * Convierte un objeto ColorRequest a un objeto Color
   *
   * @param request objeto ColorRequest a convertir
   * @return objeto Color convertido
   */
  public Color toEntity(ColorRequest request) {
    if (request == null) {
      return null;
    }

    Color color = new Color();
    color.setNombre(request.getNombre());
    color.setTallas(new HashSet<>());

    return color;
  }

  public Color toEntity(ColorRequest request, Producto producto) {
    Color color = toEntity(request);
    if (color != null) {
      color.setProducto(producto);
    }
    return color;
  }

  public void mapTallas(Color color, List<TallaRequest> tallasRequest) {
    for (TallaRequest tallaRequest : tallasRequest) {
      Talla talla = new Talla();
      talla.setNumero(tallaRequest.getNumero());
      talla.setColor(color);
      color.getTallas().add(talla);
    }
  }

  public ColorResponse toResponse(Color color) {
    if (color == null) {
      return null;
    }

    ColorResponse response = new ColorResponse();
    response.setId(color.getId());
    response.setNombre(color.getNombre());

    if (color.getTallas() != null) {
      response.setTallas(color.getTallas().stream()
              .map(this::mapTallaToResponse)
              .collect(Collectors.toList()));
    } else {
      response.setTallas(new ArrayList<>());
    }

    return response;
  }

  private TallaResponse mapTallaToResponse(Talla talla) {
    if (talla == null) {
      return null;
    }

    TallaResponse response = new TallaResponse();
    response.setId(talla.getId());
    response.setNumero(talla.getNumero());

    return response;
  }
}
