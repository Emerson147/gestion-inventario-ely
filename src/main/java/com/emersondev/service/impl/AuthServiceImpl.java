package com.emersondev.service.impl;

import com.emersondev.api.request.CambiarPasswordRequest;
import com.emersondev.api.request.LoginRequest;
import com.emersondev.api.request.RegistroRequest;
import com.emersondev.api.response.JwtResponse;
import com.emersondev.domain.entity.RefreshToken;
import com.emersondev.domain.entity.Rol;
import com.emersondev.domain.entity.Usuario;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.exception.TokenRefreshException;
import com.emersondev.domain.repository.RefreshTokenRepository;
import com.emersondev.domain.repository.RolRepository;
import com.emersondev.domain.repository.UsuarioRepository;
import com.emersondev.security.UserDetailsServiceImpl;
import com.emersondev.security.jwt.JwtProvider;
import com.emersondev.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserDetailsServiceImpl userDetailsServiceImpl;

  @Override
  @Transactional
  public JwtResponse login(LoginRequest loginRequest) {
    log.info("Iniciando proceso de login para usuario: {}", loginRequest.getUsername());

    // Autenticamos al usuario
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            )
    );

    // Establecemos la autenticación en el contexto de seguridad
    SecurityContextHolder.getContext().setAuthentication(authentication);

    // Generamos el token JWT
    String jwt = jwtProvider.generateJwtToken(authentication);

    // Obtenemos los detalles del usuario autenticado
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    // Obtenemos los roles del usuario
    List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(role -> role.replace("ROLE_", ""))
            .collect(Collectors.toList());

    // Generamos un token de refresco
    RefreshToken refreshToken = createRefreshToken(loginRequest.getUsername());

    log.info("Login exitoso para usuario: {}", loginRequest.getUsername());

    //Obenetemos el ussuario completo para acceder a su ID y email
    Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", loginRequest.getUsername()));

    // Retornamos la respuesta con el token y los datos del usuario
    return new JwtResponse(
            jwt,
            refreshToken.getToken(),
            usuario.getId(),
            userDetails.getUsername(),
            usuario.getEmail(),
            roles
    );
  }

  @Override
  @Transactional
  public JwtResponse registro(RegistroRequest registroRequest) {
    log.info("Iniciando proceso de registro para usuario: {}", registroRequest.getUsername());

    // Verificamos si el nombre de usuario ya existe
    if (usuarioRepository.existsByUsername(registroRequest.getUsername())) {
      log.error("Error en registro: el nombre de usuario {} ya está en uso", registroRequest.getUsername());
      throw new BusinessException("El nombre de usuario ya está en uso");
    }

    // Verificamos si el email ya está registrado
    if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
      log.error("Error en registro: el email {} ya está registrado", registroRequest.getEmail());
      throw new BusinessException("El email ya está registrado");
    }

    // Creamos la nueva cuenta de usuario
    Usuario usuario = new Usuario();
    usuario.setNombres(registroRequest.getNombre());
    usuario.setApellidos(registroRequest.getApellidos());
    usuario.setUsername(registroRequest.getUsername());
    usuario.setEmail(registroRequest.getEmail());
    usuario.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
    usuario.setActivo(true);

    // Asignamos el rol por defecto (VENDEDOR)
    Set<Rol> roles = new HashSet<>();
    Rol userRole = rolRepository.findByNombre(Rol.NombreRol.ROLE_VENTAS)
            .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", Rol.NombreRol.ROLE_VENTAS));
    roles.add(userRole);
    usuario.setRoles(roles);

    // Guardamos el usuario en la base de datos
    usuarioRepository.save(usuario);
    log.info("Usuario registrado exitosamente: {}", registroRequest.getUsername());

    // Procedemos con el login automático
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername(registroRequest.getUsername());
    loginRequest.setPassword(registroRequest.getPassword());

    return login(loginRequest);
  }

  @Override
  @Transactional
  public JwtResponse refreshToken(String requestRefreshToken) {
    return refreshTokenRepository.findByToken(requestRefreshToken)
            .map(this::verifyExpiration)
            .map(RefreshToken::getUsuario)
            .map(usuario -> {
              try {
                // Cargamos un objeto UserDetails usando el servicio de Spring
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(usuario.getUsername());

                // Creamos la autenticación con el UserDetails como principal
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                String jwt = jwtProvider.generateJwtToken(authentication);

                List<String> roles = usuario.getRoles().stream()
                        .map(role -> role.getNombre().name().replace("ROLE_", ""))
                        .collect(Collectors.toList());

                return new JwtResponse(
                        jwt,
                        requestRefreshToken,
                        usuario.getId(),
                        usuario.getUsername(),
                        usuario.getEmail(),
                        roles
                );
              } catch (Exception e) {
                throw new BusinessException("Error al refrescar el token: " + e.getMessage());
              }
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Token de refresco no encontrado"));
  }

  @Override
  @Transactional
  public void cambiarPassword(CambiarPasswordRequest request) {
    // Obtenemos el usuario autenticado desde el contexto de seguridad
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    log.info("Solicitud de cambio de contraseña para usuario: {}", username);

    // Buscamos al usuario por su nombre de usuario
    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

    // Verificamos que la contraseña actual sea correcta
    if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
      log.error("Contraseña actual incorrecta para usuario: {}", username);
      throw new BusinessException("La contraseña actual es incorrecta");
    }

    // Verificamos que la nueva contraseña y su confirmación coincidan
    if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
      log.error("La nueva contraseña y su confirmación no coinciden para usuario: {}", username);
      throw new BusinessException("La nueva contraseña y su confirmación no coinciden");
    }

    // Actualizamos la contraseña
    usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
    usuarioRepository.save(usuario);

    log.info("Contraseña actualizada exitosamente para usuario: {}", username);
  }

  @Override
  @Transactional
  public void logout(String token) {
    String tokenSinBearer = token.substring(7); // Eliminar el prefijo "Bearer "

    // Obtenemos el nombre de usuario del token
    String username = jwtProvider.getUsernameFromToken(tokenSinBearer);

    // Eliminamos los tokens de refresco del usuario
    refreshTokenRepository.deleteByUsuario_Username(username);

    log.info("Sesión cerrada para usuario: {}", username);
  }

  @Override
  public boolean validarToken(String token) {
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }

    return jwtProvider.validateToken(token);
  }

  /**
   * Crea un nuevo token de refresco para el usuario
   */
  @Transactional
  public RefreshToken createRefreshToken(String username) {
    // Buscamos al usuario
    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

    // Eliminamos tokens de refresco antiguos
    refreshTokenRepository.deleteByUsuario(usuario);

    // Creamos un nuevo token de refresco
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUsuario(usuario);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpirationDate(LocalDateTime.now().plusDays(30)); // 30 días

    // Guardamos el token de refresco
    return refreshTokenRepository.save(refreshToken);
  }

  /**
   * Verifica si un token de refresco ha expirado
   */
  private RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "El token de refresco ha expirado");
    }

    return token;
  }
}
