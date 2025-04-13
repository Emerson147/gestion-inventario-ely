package com.emersondev.service.interfaces;

import com.emersondev.api.request.CambiarPasswordRequest;
import com.emersondev.api.request.UsuarioRequest;
import com.emersondev.api.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

  UsuarioResponse crearUsuario(UsuarioRequest usuarioRequest);

  UsuarioResponse obtenerUsuarioPorId(Long id);

  UsuarioResponse obtenerUsuarioActual();

  List<UsuarioResponse> obtenerTodosLosUsuarios();

  UsuarioResponse actualizarUsuario(Long id, UsuarioRequest usuarioRequest);

  void cambiarPassword(CambiarPasswordRequest cambiarPasswordRequest);

  void desactivarUsuario(Long id);

  void activarUsuario(Long id);
}
