package com.emersondev.service.interfaces;

import com.emersondev.api.request.CambiarPasswordRequest;
import com.emersondev.api.request.UsuarioRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

  PagedResponse<UsuarioResponse> obtenerTodosLosUsuarios(Integer page, Integer size, String sortBy, String sortDir);

  UsuarioResponse obtenerUsuarioPorId(Long id);

  UsuarioResponse obtenerUsuarioPorUsername(String username);

  UsuarioResponse crearUsuario(UsuarioRequest usuarioRequest);

  UsuarioResponse actualizarUsuario(Long id, UsuarioRequest usuarioRequest);

  void eliminarUsuario(Long id);

  void cambiarEstadoUsuario(Long id, Boolean activo);

  UsuarioResponse actualizarRolesUsuario(Long id, List<String> roles);

  UsuarioResponse obtenerUsuarioActual();

  List<String> obtenerTodosLosRoles();

  boolean existePorUsername(String username);

  boolean existePorEmail(String email);

}
