package com.emersondev.mapper;

import com.emersondev.api.request.UsuarioRequest;
import com.emersondev.api.response.UsuarioResponse;
import com.emersondev.domain.entity.Rol;
import com.emersondev.domain.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

  public Usuario toEntity(UsuarioRequest request) {
    if (request == null) {
      return null;
    }

    Usuario usuario = new Usuario();
    usuario.setNombre(request.getNombre());
    usuario.setApellidos(request.getApellidos());
    usuario.setUsername(request.getUsername());
    usuario.setEmail(request.getEmail());
    usuario.setActivo(true);

    return usuario;
  }

  public UsuarioResponse toResponse(Usuario usuario) {
    if (usuario == null) {
      return null;
    }

    UsuarioResponse response = new UsuarioResponse();
    response.setId(usuario.getId());
    response.setNombre(usuario.getNombre());
    response.setApellido(usuario.getApellidos());
    response.setUsername(usuario.getUsername());
    response.setEmail(usuario.getEmail());
    response.setActivo(usuario.isActivo());
    response.setFechaCreacion(usuario.getFechaCreacion());
    response.setFechaActualizacion(usuario.getFechaActualizacion());

    if (usuario.getRoles() != null) {
      response.setRoles(getRolStrings(usuario.getRoles()));
    }

    return response;
  }

  public Set<String> getRolStrings(Set<Rol> roles) {
    if (roles == null) {
      return Set.of();
    }

    return roles.stream()
            .map(rol -> rol.getNombre().name().replace("ROLE_", "").toLowerCase())
            .collect(Collectors.toSet());
  }
}