package com.emersondev.api.controller;

import com.emersondev.api.request.ActualizarRolesRequest;
import com.emersondev.api.request.UsuarioRequest;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.UsuarioResponse;
import com.emersondev.service.interfaces.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

  private final UsuarioService usuarioService;

  /**
   * Obtiene todos los usuarios del sistema con paginación
   * @param page número de página (0-indexed)
   * @param size tamaño de página
   * @param sortBy campo para ordenar
   * @param sortDir dirección del ordenamiento (asc o desc)
   * @return lista paginada de usuarios
   */
  @GetMapping
  public ResponseEntity<PagedResponse<UsuarioResponse>> obtenerUsuarios(
          @RequestParam(defaultValue = "0") Integer page,
          @RequestParam(defaultValue = "10") Integer size,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir) {
    PagedResponse<UsuarioResponse> response = usuarioService.obtenerTodosLosUsuarios(page, size, sortBy, sortDir);
    return ResponseEntity.ok(response);
  }

  /**
   * Obtiene un usuario por su ID
   * @param id ID del usuario
   * @return el usuario encontrado
   */
  @GetMapping("/{id}")
  public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
    return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
  }

  /**
   * Obtiene un usuario por su nombre de usuario
   * @param username nombre de usuario
   * @return el usuario encontrado
   */
  @GetMapping("/username/{username}")
  public ResponseEntity<UsuarioResponse> obtenerUsuarioPorUsername(@PathVariable String username) {
    return ResponseEntity.ok(usuarioService.obtenerUsuarioPorUsername(username));
  }

  /**
   * Verifica si existe un usuario con el nombre de usuario especificado
   * @param username nombre de usuario a verificar
   * @return true si existe, false en caso contrario
   */
  @GetMapping("/validar-username")
  public ResponseEntity<Boolean> validarUsername(@RequestParam String username) {
    boolean existe = usuarioService.existePorUsername(username);
    return ResponseEntity.ok(existe);
  }

  /**
   * Verifica si existe un usuario con el email especificado
   * @param email email a verificar
   */
  @GetMapping("/validar-email")
  public ResponseEntity<Void> validarEmail(@RequestParam String email) {
    usuarioService.existePorEmail(email);
    return ResponseEntity.ok().build();
  }

  /**
   * Obtiene el usuario actualmente autenticado
   * @return el usuario actual
   */
  @GetMapping("/actual")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UsuarioResponse> obtenerUsuarioActual() {
    return ResponseEntity.ok(usuarioService.obtenerUsuarioActual());
  }

  /**
   * Crea un nuevo usuario
   * @param usuarioRequest datos del usuario a crear
   * @return el usuario creado
   */
  @PostMapping("/crear")
  public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest) {
    UsuarioResponse nuevoUsuario = usuarioService.crearUsuario(usuarioRequest);
    return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
  }

  /**
   * Actualiza un usuario existente
   * @param id ID del usuario
   * @param usuarioRequest datos actualizados
   * @return el usuario actualizado
   */
  @PutMapping("/actualizar/{id}")
  public ResponseEntity<UsuarioResponse> actualizarUsuario(
          @PathVariable Long id,
          @Valid @RequestBody UsuarioRequest usuarioRequest) {
    return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioRequest));
  }

  /**
   * Elimina un usuario
   * @param id ID del usuario
   * @return mensaje de confirmación
   */
  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<MensajeResponse> eliminarUsuario(@PathVariable Long id) {
    usuarioService.eliminarUsuario(id);
    return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
  }

  /**
   * Activa o desactiva un usuario
   * @param id ID del usuario
   * @param activo estado deseado (true: activar, false: desactivar)
   * @return mensaje de confirmación
   */
  @PutMapping("/{id}/cambiar-estado")
  public ResponseEntity<MensajeResponse> cambiarEstadoUsuario(
          @PathVariable Long id,
          @RequestParam Boolean activo) {
    usuarioService.cambiarEstadoUsuario(id, activo);
    return ResponseEntity.ok(new MensajeResponse(
            activo ? "Usuario activado correctamente" : "Usuario desactivado correctamente"));
  }

  /**
   * Actualiza los roles de un usuario
   * @param id ID del usuario
   * @param request request con la lista de roles
   * @return el usuario con roles actualizados
   */
  @PutMapping("/{id}/roles")
  public ResponseEntity<UsuarioResponse> actualizarRoles(
          @PathVariable Long id,
          @Valid @RequestBody ActualizarRolesRequest request) {
    return ResponseEntity.ok(usuarioService.actualizarRolesUsuario(id, request.getRoles()));
  }

  /**
   * Obtiene todos los roles disponibles en el sistema
   * @return lista de nombres de roles
   */
  @GetMapping("/roles")
  public ResponseEntity<List<String>> obtenerRoles() {
    return ResponseEntity.ok(usuarioService.obtenerTodosLosRoles());
  }
}
