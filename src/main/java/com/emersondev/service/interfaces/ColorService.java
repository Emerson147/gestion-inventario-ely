package com.emersondev.service.interfaces;

import com.emersondev.api.request.ColorRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.PagedResponse;

import java.util.List;

public interface ColorService {

  ColorResponse crearColor(Long productoId, ColorRequest colorRequest);

  PagedResponse<ColorResponse> obtenerColores(int page, int size, String sortBy, String sortDir);

  ColorResponse obtenerColorPorId(Long id);

  List<ColorResponse> obtenerColoresPorProducto(Long productoId);

  ColorResponse actualizarColor(Long id, ColorRequest colorRequest);

  void eliminarColor(Long id);
}
