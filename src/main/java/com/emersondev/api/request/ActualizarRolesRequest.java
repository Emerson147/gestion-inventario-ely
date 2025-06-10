package com.emersondev.api.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarRolesRequest {

  @NotEmpty(message = "La lista de roles no puede estar vacia")
  private List<String> roles;
}
