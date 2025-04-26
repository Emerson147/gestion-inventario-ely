package com.emersondev.mapper;

import com.emersondev.api.response.RolResponse;
import com.emersondev.domain.entity.Rol;
import org.springframework.stereotype.Component;

@Component
public class RolMapper {
  public RolResponse toResponse(Rol rol) {
    if (rol == null) {
      return null;
    }

    RolResponse response = new RolResponse();
    response.setId(rol.getId());
    response.setNombre(rol.getNombre().name().replace("ROLE_", "").toLowerCase());

    return response;
  }
}
