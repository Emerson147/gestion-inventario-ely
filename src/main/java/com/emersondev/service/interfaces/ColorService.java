package com.emersondev.service.interfaces;

import com.emersondev.api.request.ColorRequest;
import com.emersondev.api.response.ColorResponse;

import java.util.List;

public interface ColorService {

  ColorResponse crearColor(Long productoId, ColorRequest colorRequest);

  ColorResponse obtenerColorPorId(Long id);

  List<ColorResponse> obtenerColoresPorProducto(Long productoId);

  ColorResponse actualizarColor(Long id, ColorRequest colorRequest);

  void eliminarColor(Long id);
}
