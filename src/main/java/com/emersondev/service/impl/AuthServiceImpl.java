package com.emersondev.service.impl;

import com.emersondev.api.request.LoginRequest;
import com.emersondev.api.request.RegistroRequest;
import com.emersondev.api.response.JwtResponse;
import com.emersondev.domain.entity.Rol;
import com.emersondev.domain.entity.Usuario;
import com.emersondev.domain.exception.AuthException;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.TokenRefreshException;
import com.emersondev.domain.repository.RolRepository;
import com.emersondev.domain.repository.UsuarioRepository;
import com.emersondev.security.jwt.JwtProvider;
import com.emersondev.service.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;

  @Override
  @Transactional
  public JwtResponse login(LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = jwtProvider.generateJwtToken(authentication);
    String refreshToken = jwtProvider.generateJwtToken(authentication);

    org.springframework.security.core.userdetails.UserDetails userDetails =
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

    List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new AuthException("Usuario no encontrado"));

    return new JwtResponse(
            jwt,
            refreshToken,
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            roles
    );
  }

  @Override
  @Transactional
  public JwtResponse registro(RegistroRequest registroRequest) {
    if (usuarioRepository.existsByUsername(registroRequest.getUsername())) {
      throw new BusinessException("El nombre de usuario ya está en uso");
    }

    if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
      throw new BusinessException("El email ya está en uso");
    }

    Usuario usuario = new Usuario();
    usuario.setNombre(registroRequest.getNombre());
    usuario.setApellidos(registroRequest.getApellidos());
    usuario.setUsername(registroRequest.getUsername());
    usuario.setEmail(registroRequest.getEmail());
    usuario.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
    usuario.setActivo(true);

    Set<String> strRoles = registroRequest.getRoles();
    Set<Rol> roles = new HashSet<>();

    if (strRoles == null || strRoles.isEmpty()) {
      Rol userRole = rolRepository.findByNombre(Rol.NombreRol.ROLE_VENDEDOR)
              .orElseThrow(() -> new BusinessException("Error: Rol no encontrado"));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "admin":
            Rol adminRole = rolRepository.findByNombre(Rol.NombreRol.ROLE_ADMIN)
                    .orElseThrow(() -> new BusinessException("Error: Rol ADMIN no encontrado"));
            roles.add(adminRole);
            break;
          case "inventario":
            Rol modRole = rolRepository.findByNombre(Rol.NombreRol.ROLE_INVENTARIO)
                    .orElseThrow(() -> new BusinessException("Error: Rol INVENTARIO no encontrado"));
            roles.add(modRole);
            break;
          default:
            Rol userRole = rolRepository.findByNombre(Rol.NombreRol.ROLE_VENDEDOR)
                    .orElseThrow(() -> new BusinessException("Error: Rol VENDEDOR no encontrado"));
            roles.add(userRole);
        }
      });
    }

    usuario.setRoles(roles);
    usuarioRepository.save(usuario);
    // Generar el token JWT después de registrar al usuario
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername(registroRequest.getUsername());
    loginRequest.setPassword(registroRequest.getPassword());

    return login(loginRequest);



//     Crear una respuesta personalizada con la información básica del usuario
//    List<String> roleNames = usuario.getRoles().stream()
//            .map(rol -> rol.getNombre().name())
//            .collect(Collectors.toList());
//
//    return new JwtResponse(
//            null,  // sin token de acceso
//            null,  // sin refresh token
//            usuario.getId(),
//            usuario.getUsername(),
//            usuario.getEmail(),
//            roleNames
//    );


//
//    List<String> roleNames = usuario.getRoles().stream()
//            .map(rol -> rol.getNombre().name())
//            .collect(Collectors.toList());
//
//    return new JwtResponse(
//            jwt,
//            refreshToken,
//            usuario.getId(),
//            usuario.getUsername(),
//            usuario.getEmail(),
//            roleNames
//    );
  }

  @Override
  @Transactional
  public JwtResponse refreshToken(String refreshToken) {
    try {
      if (!jwtProvider.validateRefreshToken(refreshToken)) {
        throw new TokenRefreshException(refreshToken, "Token de refresco inválido");
      }

      String username = jwtProvider.getUsernameFromRefreshToken(refreshToken);
      Usuario usuario = usuarioRepository.findByUsername(username)
              .orElseThrow(() -> new TokenRefreshException(refreshToken, "Usuario no encontrado"));

      Authentication authentication = jwtProvider.getAuthenticationFromUsername(username);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      String newToken = jwtProvider.generateJwtToken(authentication);
      String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

      List<String> roles = authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

      return new JwtResponse(
              newToken,
              newRefreshToken,
              usuario.getId(),
              usuario.getUsername(),
              usuario.getEmail(),
              roles
      );
    } catch (Exception e) {
      throw new TokenRefreshException(refreshToken, e.getMessage());
    }
  }

  @Override
  @Transactional
  public void logout() {
    // En una implementación JWT básica, el logout se maneja en el cliente eliminando el token
    // Este método se podría extender para incluir una lista de tokens inválidos (blacklist)
    SecurityContextHolder.clearContext();
  }
}
