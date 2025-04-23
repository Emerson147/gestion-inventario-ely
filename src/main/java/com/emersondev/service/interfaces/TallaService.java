package com.emersondev.service.interfaces;

import com.emersondev.api.request.TallaRequest;
import com.emersondev.api.response.TallaResponse;

import java.util.List;

public interface TallaService {

  TallaResponse crearTalla(Long colorId, TallaRequest tallaRequest);

  TallaResponse obtenerTallaPorId(Long id, Long colorId);

  List<TallaResponse> obtenerTallasPorColor(Long colorId);

  TallaResponse actualizarTalla(Long id, TallaRequest tallaRequest);

  void eliminarTalla(Long id);
}
