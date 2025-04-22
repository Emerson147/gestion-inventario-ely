package com.emersondev.service.impl;

import com.emersondev.api.request.UsuarioRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.UsuarioResponse;
import com.emersondev.domain.entity.Rol;
import com.emersondev.domain.entity.Usuario;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.RolRepository;
import com.emersondev.domain.repository.UsuarioRepository;
import com.emersondev.mapper.UsuarioMapper;
import com.emersondev.service.interfaces.UsuarioService;
import com.emersondev.util.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final UsuarioMapper usuarioMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UsuarioResponse crearUsuario(UsuarioRequest usuarioRequest) {
    log.info("Creando usuario: {}", usuarioRequest.getUsername());

    //Verificar si el nombre del usuario ya existe
    if (usuarioRepository.existsByUsername(usuarioRequest.getUsername())) {
      log.error("El nombre de usuario {} ya existe", usuarioRequest.getUsername());
      throw new BusinessException("El nombre de usuario ya existe");
    }

    //Verificar si el email ya esta registrado
    if (usuarioRepository.existsByEmail(usuarioRequest.getEmail())) {
      log.error("El email {} ya esta registrado", usuarioRequest.getEmail());
      throw new BusinessException("El email ya esta registrado");
    }

    //Creear un nuevo Usuario
    Usuario usuario = usuarioMapper.toEntity(usuarioRequest);
    usuario.setPassword(passwordEncoder.encode(usuarioRequest.getPassword()));
    usuario.setActivo(true);

    //Asignar roles
    Set<Rol> roles = new HashSet<>();

    if (usuarioRequest.getRoles() != null && !usuarioRequest.getRoles().isEmpty()) {
      for (String rolNombre : usuarioRequest.getRoles()) {
        Rol.NombreRol nombre = Rol.NombreRol.valueOf("ROLE_" + rolNombre.toUpperCase());
        Rol rol = rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", rolNombre));
        roles.add(rol);
      }
    } else {
      //Si no se especifican roles, asignar el rol de USUARIO por defecto
      Rol rol = rolRepository.findByNombre(Rol.NombreRol.ROLE_VENDEDOR)
              .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", "ROLE_USER"));
      roles.add(rol);
    }

    usuario.setRoles(roles);

    //Guardar el usuario
    usuario = usuarioRepository.save(usuario);
    log.info("Usuario creado exitosamente con ID: {}", usuario.getId());

    return usuarioMapper.toResponse(usuario);
  }

  @Override
  @Transactional
  public UsuarioResponse obtenerUsuarioPorId(Long id) {
    log.info("Obteniendo usuario por ID: {}", id);
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Usuario no encontrado con ID: {}", id);
              return new ResourceNotFoundException("Usuario", "id", id);
            });
    return usuarioMapper.toResponse(usuario);
  }

  @Override
  @Transactional
  public PagedResponse<UsuarioResponse> obtenerTodosLosUsuarios(Integer page, Integer size, String sortBy, String sortDir) {
    log.debug("Obteniendo pagina {} de usuarios con tamaño {} ordenada por {} en dirección {}", page, size, sortBy, sortDir);

    //Validar parametros de paginacion
    int[] validatedParams = PaginationUtils.validatePaginationParams(page, size);
    page = validatedParams[0];
    size = validatedParams[1];

    //Crear el obejto pageable
    Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);

    //Ejecutar la consulata paginada
    Page<Usuario> usuariosPage = usuarioRepository.findAll(pageable);

    //Si no hay resultados, devolver una respuesta vacia
    if (usuariosPage.isEmpty()) {
      log.info("No se encontraron usuarios");
      return PaginationUtils.emptyPagedResponse(page, size);
    }

    //Convertir y devolver las respuestas paginadas
    return PaginationUtils.createPagedResponse(usuariosPage, usuarioMapper::toResponse);

  }

  @Override
  @Transactional
  public UsuarioResponse obtenerUsuarioPorUsername(String username) {
    log.debug("Obteniendo usuarios por username: {}", username);

    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> {
              log.error("Usuario no encontrado con username: {}", username);
              return new ResourceNotFoundException("Usuario", "username", username);
            });

    log.info("Usuario encontrado: {}", usuario.getUsername());
    return usuarioMapper.toResponse(usuario);
  }

  @Override
  @Transactional
  public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest usuarioRequest) {
    log.info("Actualizando usuario con ID: {}", id);
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Usuario no encontrado con ID: {}", id);
              return new ResourceNotFoundException("Usuario", "id", id);
            });

    //Verificar si el nombre de usuario ya esta en uso por otro usuario
    if (!usuario.getUsername().equals(usuarioRequest.getUsername()) && usuarioRepository.existsByUsername(usuarioRequest.getUsername())) {
      log.error("El nombre de usuario {} ya esta en uso", usuarioRequest.getUsername());
      throw new BusinessException("El nombre de usuario ya esta en uso");
    }

    // Verificar si el email ya esta en uso por otro usuario
    if (!usuario.getEmail().equals(usuarioRequest.getEmail()) && usuarioRepository.existsByEmail(usuarioRequest.getEmail())) {
      log.error("El email {} ya esta registrado", usuarioRequest.getEmail());
      throw new BusinessException("El email ya esta en uso");
    }

    //Actualizar datos del usuario
    usuario.setNombre(usuarioRequest.getNombre());
    usuario.setApellidos(usuarioRequest.getApellidos());
    usuario.setUsername(usuarioRequest.getUsername());
    usuario.setEmail(usuarioRequest.getEmail());

    // Actualizar password solo si se proporciona uno nuevo
    if (usuarioRequest.getPassword() != null && !usuarioRequest.getPassword().isEmpty()) {
      usuario.setPassword(passwordEncoder.encode(usuarioRequest.getPassword()));
    }

    // Actualizar roles si se proporciona
    if (usuarioRequest.getRoles() != null && !usuarioRequest.getRoles().isEmpty()) {
      Set<Rol> roles = new HashSet<>();
      for (String rolNombre : usuarioRequest.getRoles()) {
        Rol.NombreRol nombre = Rol.NombreRol.valueOf("ROLE_" + rolNombre.toUpperCase());
        Rol rol = rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", rolNombre));
        roles.add(rol);
      }
      usuario.setRoles(roles);
    }

    // Guardar los cambios
    usuario = usuarioRepository.save(usuario);
    log.info("Usuario actualizado exitosamente con ID: {}", usuario.getId());

    return usuarioMapper.toResponse(usuario);
  }

  @Override
  @Transactional
  public void eliminarUsuario(Long id) {
    log.info("Eliminando usuario con ID: {}", id);

    if (!usuarioRepository.existsById(id)) {
      log.error("Usuario no encontrado con ID: {}", id);
      throw new ResourceNotFoundException("Usuario", "id", id);
    }

    usuarioRepository.deleteById(id);
    log.info("Usuario eliminado exitosamente con ID: {}", id);
  }

  @Override
  public void cambiarEstadoUsuario(Long id, Boolean activo) {
    log.info("Cambiando estado de usuario con ID: {}", id);

    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Usuario no encontrado con ID: {}", id);
              return new ResourceNotFoundException("Usuario", "id", id);
            });

    usuario.setActivo(activo);
    usuarioRepository.save(usuario);
    log.info("Estado de usuario cambiado exitosamente con ID: {}, activo: {}", id, activo);
  }

  @Override
  @Transactional
  public UsuarioResponse actualizarRolesUsuario(Long id, List<String> roles) {
    log.info("Actualizando roles de usuario con ID: {}", id);

    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Usuario no encontrado con ID: {}", id);
              return new ResourceNotFoundException("Usuario", "id", id);
            });

    // Actualizar roles
    if (roles != null && !roles.isEmpty()) {
      Set<Rol> rolesEntidad = new HashSet<>();

      for (String roleNombre : roles) {
        Rol.NombreRol nombre = Rol.NombreRol.valueOf("ROLE_" + roleNombre.toUpperCase());
        Rol rol = rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", roleNombre));
        rolesEntidad.add(rol);
      }

      usuario.setRoles(rolesEntidad);
      usuario = usuarioRepository.save(usuario);

      log.info("Roles de usuario actualizados exitosamente con ID: {}", id);
    }
    return  usuarioMapper.toResponse(usuario);
  }

  @Override
  public UsuarioResponse obtenerUsuarioActual() {
    log.debug("Obteniendo información del usuario actual autenticado");

    // Obtener el nombre de usuario del contexto de seguridad
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    // Buscar el usuario en la base de datos
    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> {
              log.error("Usuario actual no encontrado con username: {}", username);
              return new ResourceNotFoundException("Usuario", "username", username);
            });

    log.info("Usuario actual encontrado: {}", usuario.getUsername());
    return usuarioMapper.toResponse(usuario);
  }

  @Override
  @Transactional
  public List<String> obtenerTodosLosRoles() {
    log.debug("Obteniendo todos los roles disponibles");

    return Arrays.stream(Rol.NombreRol.values())
            .map(rol -> rol.name().replace("ROLE_", "").toUpperCase())
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean existePorUsername(String username) {
    return usuarioRepository.existsByUsername(username);
  }

  @Override
  @Transactional
  public boolean existePorEmail(String email) {
    return usuarioRepository.existsByEmail(email);
  }



}
